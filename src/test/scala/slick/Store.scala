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
    val action = for { p <- persons } yield p
    db.run(action.result)
  }

  def listTasks(person: Person): Future[Seq[Task]] = {
    val action = for { t <- tasks if t.id === person.id } yield t
    db.run(action.result)
  }

  def findPerson(name: String): Future[Person] = {
    val action = persons.filter(_.name === name).result.head
    db.run(action)
  }

  def upsert(person: Person): Future[Int] = {
    val action = persons.insertOrUpdate(person)
    db.run(action)
  }

  def upsert(task: Task): Future[Int] = {
    val action = tasks.insertOrUpdate(task)
    db.run(action)
  }
}