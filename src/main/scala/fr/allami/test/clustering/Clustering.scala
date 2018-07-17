package fr.allami.test.clustering

import fr.allami.test.config._
import org.apache.spark.ml.feature.{IndexToString, StringIndexer}
import org.apache.spark.mllib.clustering.BisectingKMeansModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.mllib.clustering.BisectingKMeans
import org.apache.spark.mllib.linalg.{Vector, Vectors}

class Clustering {

  def readJsonFile(spark: SparkSession,
                   input: Option[String] = None): DataFrame = {

    val resourcesPath = getClass.getResource(Settings.config.input)
    val inputJson: String = input.getOrElse(resourcesPath.getPath)
    val data = spark.read
      .option("multiline", "true")
      .json(inputJson)
    data
  }

  /**
    *
    * @param spark
    */
  def run(spark: SparkSession,
          input: Option[String] = None,
          output: Option[String] = None): Unit = {
    import org.apache.spark.sql.functions._
    val vectors = toVector(prepare(readJsonFile(spark, input)))
    val model = build(vectors)
    //  println(InverseIndexer(prepare(readJsonFile(spark))).show(34))
    val out: String = output.getOrElse("/tmp/cluster-")
    val inversedIndexer = InverseIndexer(prepare(readJsonFile(spark, input)))
      .withColumn("id", monotonicallyIncreasingId)
    predict(vectors, model).collect().map {
      case (cluster, instances) => {
        instances.foreach { instance =>
          inversedIndexer
            .where(col("id") === instance)
            .coalesce(1)
            .write
            .mode(SaveMode.Append)
            .json(out + cluster)

        }
      }

    }

  }

  /**
    * build a clustering model
    * @param data
    * @return
    */
  def build(data: RDD[Vector]): BisectingKMeansModel = {
    val model = new BisectingKMeans().setK(6).setMaxIterations(20)
    model.run(data)
  }

  /**
    * preparation of data by removing unused data and converting categorical features
    * @param data
    * @return
    */
  def prepare(data: DataFrame): DataFrame = {
    val dataDf = data.drop("_corrupt_record")
    val cleanDf =
      dataDf
        .filter(
          col("address").isNotNull && col("name").isNotNull &&
            col("number").isNotNull && col("latitude").isNotNull && col(
            "longitude").isNotNull)
        .select("address", "name", "number", "latitude", "longitude")

    IndexerString(cleanDf)
  }

  /**
    * converting categorical feature to double
    * @param data
    * @return
    */
  def IndexerString(data: DataFrame): DataFrame = {
    val nameIndexer =
      new StringIndexer()
        .setInputCol("name")
        .setOutputCol("nameIndex")
        .setHandleInvalid("skip")
    val indexedData = nameIndexer.fit(data).transform(data)

    val addressIndexer = new StringIndexer()
      .setInputCol("address")
      .setOutputCol("addressIndex")
      .setHandleInvalid("skip")

    val preparedData = addressIndexer.fit(indexedData).transform(indexedData)
    preparedData.select("addressIndex",
                        "nameIndex",
                        "number",
                        "latitude",
                        "longitude")

  }

  /**
    * returning back the inital features
    * @param data
    * @return
    */
  def InverseIndexer(data: DataFrame): DataFrame = {
    val AddressConverter = new IndexToString()
      .setInputCol("addressIndex")
      .setOutputCol("address")
    val converted = AddressConverter.transform(data)

    val nameConverter = new IndexToString()
      .setInputCol("nameIndex")
      .setOutputCol("name")
    nameConverter
      .transform(converted)
      .select("address", "name", "number", "latitude", "longitude")
  }

  /**
    * converting to vector
    * @param data
    * @return
    */
  def toVector(data: DataFrame): RDD[Vector] = {
    data
      .withColumn("addressIndex", col("addressIndex").cast("double"))
      .withColumn("number", col("number").cast("double"))
      .withColumn("nameIndex", col("nameIndex").cast("double"))
      .withColumn("latitude", col("latitude").cast("double"))
      .withColumn("longitude", col("longitude").cast("double"))
      .select("addressIndex", "nameIndex", "number", "latitude", "longitude")
      .rdd
      .map(
        r =>
          Vectors.dense(r.getDouble(0),
                        r.getDouble(1),
                        r.getDouble(2),
                        r.getDouble(3),
                        r.getDouble(4)))

  }

  /**
    * passing data to the model to generate clustering
    *
    * @param data
    * @param model
    * @return
    */
  def predict(data: RDD[Vector], model: BisectingKMeansModel) = {

    model
      .predict(data)
      .zipWithIndex()
      .map(tuple => (tuple._1, tuple._2))
      .groupByKey()

  }

}
