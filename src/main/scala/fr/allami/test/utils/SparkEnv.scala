package fr.allami.test.utils

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

class SparkEnv(name: String, extraConfKeys: Set[String] = Set()) {

  val log = LoggerFactory.getLogger(classOf[SparkEnv])

  protected val config: Config =
    ConfigFactory.load("spark").withFallback(ConfigFactory.load())

  private val sparkConfig = {
    val self = new SparkConf().setAppName(name)
    val allConfKeys = extraConfKeys.toList :+ "spark"

    allConfKeys.foreach(
      confKey =>
        config
          .getConfig(confKey)
          .entrySet()
          .map(x => (x.getKey, x.getValue.unwrapped().toString))
          .foreach {
            case (key, value) =>
              self.set(confKey + "." + key, value)
        }
    )
    self
  }

  protected def builder = SparkSession.builder.config(sparkConfig)

  /**
    * The SparkSession managed by this environment
    */
  lazy val spark: SparkSession = builder.getOrCreate()

}
