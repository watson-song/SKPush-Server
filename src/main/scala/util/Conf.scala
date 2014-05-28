package util

import com.typesafe.config.ConfigFactory
import scala.util.Properties

object Conf {

  val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val appHostName = config.getString("spush.app.hostname")
  val appPort = Properties.envOrElse("PORT", config.getString("spush.app.port")).toInt

  val apiUrl = config.getString("spush.api.url")

  val dbUsername = config.getString("spush.db.username")
  val dbPassword = config.getString("spush.db.password")
  val dbPort = config.getInt("spush.db.port")
  val dbName = config.getString("spush.db.name")

  val dbPoolMaxObjects = config.getInt("spush.db.pool.maxObjects")
  val dbPoolMaxIdle = config.getInt("spush.db.pool.maxIdle")
  val dbPoolMaxQueueSize = config.getInt("spush.db.pool.maxQueueSize")
}
