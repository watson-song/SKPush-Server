package db

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.github.mauricio.async.db.{ RowData, QueryResult }

trait DB {
  val pool = Pool.pool

  /**
   * Creates a prepared statement with the given query
   * and passes it to the connection pool with given values.
   */
  def execute(query: String, values: Any*): Future[QueryResult] = {
    if (values.size > 0)
      pool.sendPreparedStatement(query, values)
    else
      pool.sendQuery(query)
  }

  /**
   * Creates a prepared statement with the given query
   * and passes it to the connection pool with given values.
   * @return Seq[RowData] of the query
   */
  def fetch(query: String, values: Any*): Future[Option[Seq[RowData]]] =
    execute(query, values: _*).map(_.rows)

}
