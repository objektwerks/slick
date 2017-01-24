package objektwerks

import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration._

class Repository(db: Database, awaitDuration: Duration) extends Schema {
  def exec[T](action: DBIO[T]): T = Await.result(db.run(action), awaitDuration)

  def close() = db.close()
}