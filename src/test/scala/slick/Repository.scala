package slick

import java.sql.Timestamp
import java.time.LocalDateTime

import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class Repository(db: Database) {
  private implicit val LocalDateTimeMapper = MappedColumnType.base[LocalDateTime, Timestamp](l => Timestamp.valueOf(l), t => t.toLocalDateTime)
  private val persons = TableQuery[Persons]
  private val tasks = TableQuery[Tasks]
  private val schema = persons.schema ++ tasks.schema

  def await[T](future: Future[T], duration: Duration): T = Await.result(future, duration)

  def createSchema(): Future[Unit] = db.run(DBIO.seq(schema.create))

  def dropSchema(): Future[Unit] = db.run(DBIO.seq(schema.drop))

  def closeDatabase(): Unit = db.close()

  def addPerson(name: String): Future[Int] = db.run((persons returning persons.map(_.id)) += Person(name = name))

  def addTask(personId: Int, task: String): Future[Int] = db.run((tasks returning tasks.map(_.id)) += Task(personId = personId, task = task))

  def updateTask(task: Task): Future[Int] = db.run(tasks.insertOrUpdate(task))

  def findPerson(name: String): Future[Person] = db.run(persons.filter(_.name === name).result.head)

  def listPersons(): Future[Seq[Person]] = db.run(persons.sortBy(_.name.asc).result)

  def listTasks(person: Person): Future[Seq[Task]] = db.run(tasks.filter(_.id === person.id).sortBy(_.assigned.asc).result)

  def listPersonsTasks(): Future[Seq[(String, String)]] = {
    val query = for {
      p <- persons
      t <- tasks if p.id === t.personId
    } yield (p.name, t.task)
    db.run(query.result)
  }

  case class Person(id: Option[Int] = None, name: String)

  class Persons(tag: Tag) extends Table[Person](tag, "persons") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id.?, name) <> (Person.tupled, Person.unapply)
  }

  case class Task(id: Option[Int] = None, personId: Int, task: String, assigned: LocalDateTime = LocalDateTime.now, completed: Option[LocalDateTime] = None)

  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def personId = column[Int]("person_id")
    def task = column[String]("task")
    def assigned = column[LocalDateTime]("assigned")
    def completed = column[Option[LocalDateTime]]("completed")
    def * = (id.?, personId, task, assigned, completed) <> (Task.tupled, Task.unapply)
    def person = foreignKey("person_fk", personId, TableQuery[Persons])(_.id)
  }
}