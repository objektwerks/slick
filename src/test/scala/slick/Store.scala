package slick

import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Store extends Repository {
  val db = Database.forConfig("test")

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