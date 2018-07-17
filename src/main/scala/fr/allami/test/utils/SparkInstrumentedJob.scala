package fr.allami.test.utils

import org.apache.spark.sql.SparkSession

trait SparkInstrumentedJob {

  def additionalTags: Map[String, String] = Map()

  def sparkExtraParams: Set[String] = Set()

  def jobName: String

  lazy val spark: SparkSession = new SparkEnv(jobName, sparkExtraParams).spark

  def run(args: Array[String]): Unit

  def main(args: Array[String]): Unit = {
    try {
      run(args)
    } finally {
      spark.stop()
    }
  }
}
