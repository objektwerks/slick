package slick

import java.sql.Timestamp

import slick.jdbc.H2Profile.api._

trait Repository {
  val persons = TableQuery[Persons]
  val tasks = TableQuery[Tasks]
  val schema = persons.schema ++ tasks.schema
  val createSchema = DBIO.seq(schema.create)
  val dropSchema = DBIO.seq(schema.drop)

  def upsert(person: Person) = persons.insertOrUpdate(person)
  def upsert(task: Task) = tasks.insertOrUpdate(task)
  def findPerson(name: String) = persons.filter(_.name === name).result.head
  def listPersons = persons.sortBy(_.name.asc).result
  def listTasks(person: Person) = tasks.filter(_.id === person.id).sortBy(_.timestamp.asc).result

  case class Person(id: Option[Int] = None, name: String)

  class Persons(tag: Tag) extends Table[Person](tag, "persons") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id.?, name) <> (Person.tupled, Person.unapply)
  }

  case class Task(id: Option[Int] = None, personId: Int, task: String, timestamp: Timestamp = new Timestamp(System.currentTimeMillis))

  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def personId = column[Int]("person_id")
    def task = column[String]("task")
    def timestamp = column[Timestamp]("timestamp")
    def * = (id.?, personId, task, timestamp) <> (Task.tupled, Task.unapply)
    def person = foreignKey("person_fk", personId, TableQuery[Persons])(_.id)
  }
}