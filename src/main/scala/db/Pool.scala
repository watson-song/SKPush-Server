package db

import com.github.mauricio.async.db.Configuration
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.ConnectionPool
import com.github.mauricio.async.db.pool.PoolConfiguration
import util.Conf

object Pool {

  val configuration = new Configuration(username = Conf.dbUsername,
    port = Conf.dbPort,
    password = Some(Conf.dbPassword),
    database = Some(Conf.dbName))

  val factory = new MySQLConnectionFactory(configuration)
  val pool = new ConnectionPool(factory, new PoolConfiguration(Conf.dbPoolMaxObjects, Conf.dbPoolMaxIdle, Conf.dbPoolMaxQueueSize))

}