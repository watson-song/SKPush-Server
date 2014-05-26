package util

import com.typesafe.config.ConfigFactory
import scala.util.Properties

object Conf {

  val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val appHostName = config.getString("tcp-async.app.hostname")
  val appPort = Properties.envOrElse("PORT", config.getString("tcp-async.app.port")).toInt

  val apiUrl = config.getString("tcp-async.api.url")

  val dbUsername = config.getString("tcp-async.db.username")
  val dbPassword = config.getString("tcp-async.db.password")
  val dbPort = config.getInt("tcp-async.db.port")
  val dbName = config.getString("tcp-async.db.name")

  val dbPoolMaxObjects = config.getInt("tcp-async.db.pool.maxObjects")
  val dbPoolMaxIdle = config.getInt("tcp-async.db.pool.maxIdle")
  val dbPoolMaxQueueSize = config.getInt("tcp-async.db.pool.maxQueueSize")
}
