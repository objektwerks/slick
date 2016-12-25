package slick

import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Store {
  private val persons = TableQuery[Persons]
  private val tasks = TableQuery[Tasks]
  private val db = Database.forConfig("test")


  def createSchema(): Unit = {
    val schema = DBIO.seq( ( persons.schema ++ tasks.schema ).create )
    val future = db.run(schema)
    Await.ready(future, Duration.Inf)
  }

  def dropSchema(): Unit = {
    val schema = DBIO.seq( ( persons.schema ++ tasks.schema ).drop )
    val future = db.run(schema)
    Await.ready(future, Duration.Inf)
  }

  def close(): Unit = {
    db.close()
  }

  def listPersons: Future[Seq[Person]] = {
    val query = for { p <- persons } yield p
    db.run(query.result)
  }

  def listTasks(person: Person): Future[Seq[Task]] = {
    val query = for { t <- tasks if t.id === person.id } yield t
    db.run(query.result)
  }

  def findPerson(name: String): Future[Person] = {
    val query = persons.filter(_.name === name).result.head
    db.run(query)
  }

  def insert(person: Person): Future[Int] = {
    val query = persons += person
    db.run(query)
  }

  def insert(task: Task): Future[Int] = {
    val query = tasks += task
    db.run(query)
  }
}