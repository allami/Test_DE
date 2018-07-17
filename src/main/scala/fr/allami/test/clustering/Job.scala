package fr.allami.test.clustering

import fr.allami.test.utils.{SparkEnv, SparkInstrumentedJob}
//import org.apache.log4j.{Level, Logger}

object Job extends SparkInstrumentedJob {

  // Logger.getLogger("org").setLevel(Level.OFF)
  // Logger.getLogger("akka").setLevel(Level.OFF)

  override def jobName = "Clustering job"

  def run(args: Array[String]) = {

    // Logger.getLogger("org").setLevel(Level.OFF)
    if (args.length > 1)
      new Clustering().run(spark, Some(args(0)), Some(args(1)))
    else
      new Clustering().run(spark, None, None)
  }
}
