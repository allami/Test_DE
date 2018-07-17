package fr.allami.test.clustering

import fr.allami.test.utils.{SparkEnv, SparkInstrumentedJob}
//import org.apache.log4j.{Level, Logger}

object Job extends SparkInstrumentedJob {

  // Logger.getLogger("org").setLevel(Level.OFF)
  // Logger.getLogger("akka").setLevel(Level.OFF)

  override def jobName = "Clustering job"

  def run(args: Array[String]) = {

    // Logger.getLogger("org").setLevel(Level.OFF)

    val cluster = new Clustering().run(spark)
  }
}
