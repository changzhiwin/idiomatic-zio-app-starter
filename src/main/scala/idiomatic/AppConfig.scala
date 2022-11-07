package idiomatic

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import zio._

object AppConfig {

  /**
    * Ref: https://lightbend.github.io/config/latest/api/
    * ConfigFactory.load() loads the following (first-listed are higher priority)
    * system properties
    * application.conf (all resources on classpath with this name)
    * application.json (all resources on classpath with this name)
    * application.properties (all resources on classpath with this name)
    * reference.conf (all resources on classpath with this name)
    */

  val layer = ZLayer.succeed( ConfigFactory.load() )

  /**
    * Copy from quill-jdbc/src/main/scala/io/getquill/JdbcContextConfig.scala
    * Use for zio-config, if need
    * ZConfig.fromProperties(configProperties, AppConfig.configuration, "constant")
    */
  def configProperties: Properties = {
    import scala.jdk.CollectionConverters._
    val p = new Properties
    val config: Config = ConfigFactory.load()

    for (entry <- config.entrySet.asScala)
      p.setProperty(entry.getKey, entry.getValue.unwrapped.toString)
    p
  }
}