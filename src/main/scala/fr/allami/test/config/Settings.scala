package fr.allami.test.config

import com.typesafe.config.{Config, ConfigFactory}
import configs.{ConfigError, Configs}
import org.slf4j.{Logger, LoggerFactory}

final case class deConf(input: String, output: String)

object Settings {

  val log: Logger = LoggerFactory.getLogger(getClass)

  val conf: Config = ConfigFactory.load()

  val config =
    Configs[deConf].get(conf, "de").valueOrThrow(configErrorsToException)

  def configErrorsToException(err: ConfigError) =
    new IllegalStateException(err.entries.map(_.messageWithPath).mkString(","))
}
