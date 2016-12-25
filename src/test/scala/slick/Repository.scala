package slick

import slick.jdbc.H2Profile.api._

trait Repository {
  val persons = TableQuery[Persons]
  val tasks = TableQuery[Tasks]
  val schema = persons.schema ++ tasks.schema

  def listPersons = ( for { p <- persons } yield p ).result
  def listTasks(person: Person) = ( for { t <- tasks if t.id === person.id } yield t ).result
  def findPerson(name: String) = persons.filter(_.name === name).result.head
  def upsert(person: Person) = persons.insertOrUpdate(person)
  def upsert(task: Task) = tasks.insertOrUpdate(task)

  case class Person(id: Option[Int] = None, name: String)

  class Persons(tag: Tag) extends Table[Person](tag, "persons") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id.?, name) <> (Person.tupled, Person.unapply)
  }

  case class Task(id: Option[Int] = None, personId: Int, task: String)

  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def person_id = column[Int]("person_id")
    def task = column[String]("task")
    def * = (id.?, person_id, task) <> (Task.tupled, Task.unapply)
    def person = foreignKey("person_fk", person_id, TableQuery[Persons])(_.id)
  }
}