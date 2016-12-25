package slick

import com.typesafe.config.Config
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class Store(conf: Config, store: String) extends Repository {
  val db = Database.forConfig(store, conf)

  def createSchema(): Unit = {
    val schema = (persons.schema ++ tasks.schema).create
    val ddl = DBIO.seq(schema)
    val future = db.run(ddl)
    Await.ready(future, Duration.Inf)
  }

  def dropSchema(): Unit = {
    val schema = (persons.schema ++ tasks.schema).drop
    val ddl = DBIO.seq(schema)
    val future = db.run(ddl)
    Await.ready(future, Duration.Inf)
  }

  def close(): Unit = {
    db.close()
  }
}