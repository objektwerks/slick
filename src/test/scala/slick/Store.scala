package slick

import com.typesafe.config.Config
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class Store(conf: Config, store: String) extends Repository {
  val db = Database.forConfig(store, conf)

  def createSchema(): Unit = {
    val ddl = DBIO.seq(schema.create)
    val future = db.run(ddl)
    Await.ready(future, Duration.Inf)
  }

  def dropSchema(): Unit = {
    val ddl = DBIO.seq(schema.drop)
    val future = db.run(ddl)
    Await.ready(future, Duration.Inf)
  }

  def close(): Unit = {
    db.close()
  }
}