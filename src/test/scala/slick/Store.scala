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
    db.run(persons.filter(_.name === name).result.head)
  }

  def insert(person: Person): Future[Int] = {
    db.run( (persons returning persons.map(_.id)) forceInsert person )
  }

  def insert(task: Task): Future[Int] = {
    db.run( (tasks returning tasks.map(_.id)) forceInsert task )
  }
}