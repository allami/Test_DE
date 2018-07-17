package fr.allami.test.clustering

import com.typesafe.scalalogging.{LazyLogging, StrictLogging}
import fr.allami.test.config._
import org.apache.spark
import org.apache.spark.ml.feature.{IndexToString, Normalizer, StringIndexer}
import org.apache.spark.sql
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.Column
import org.apache.spark.sql.types.{DataType, DateType, TimestampType}
import org.apache.spark.sql.functions.{col, udf}
import fr.allami.test.config.Settings._
import org.apache.spark.mllib.clustering.BisectingKMeansModel
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
// $example on$
import org.apache.spark.mllib.clustering.BisectingKMeans
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.ml.feature.StandardScaler

class Clustering extends LazyLogging {

  def readJsonFile(spark: SparkSession): DataFrame = {
    logger.debug("read json file ")
    val inputJson = Settings.config.input
    val data = spark.read
      .option("multiline", "true")
      .json("/Users/allami/Downloads/Test_DE/Brisbane_CityBike.json")
    data
  }

  /**
    *
    * @param spark
    */
  def run(spark: SparkSession): Unit = {
    import org.apache.spark.sql.functions._
    val vectors = toVector(prepare(readJsonFile(spark)))
    val model = build(vectors)
    //  println(InverseIndexer(prepare(readJsonFile(spark))).show(34))

    predict(vectors, model).saveAsTextFile("/tmp/out")

    println(
      InverseIndexer(prepare(readJsonFile(spark)))
        .withColumn("id", monotonicallyIncreasingId)
        .where(col("id") === 1)
        .show(34))
  }

  /**
    *
    * @param data
    * @return
    */
  def build(data: RDD[Vector]): BisectingKMeansModel = {
    val model = new BisectingKMeans().setK(6).setMaxIterations(20)
    model.run(data)
  }

  /**
    *
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
    *
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
    *
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
    *
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

  def save(cluster: Int, data: RDD[Iterable[Long]], path: String): Unit = {
    data.saveAsTextFile(path)
  }
}
