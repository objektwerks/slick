package slick

import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class Repository(db: Database) extends Schema {
  def await[T](future: Future[T], duration: Duration): T = Await.result(future, duration)

  def createSchema(): Future[Unit] = db.run(DBIO.seq(schema.create))

  def dropSchema(): Future[Unit] = db.run(DBIO.seq(schema.drop))

  def closeDatabase(): Unit = db.close()

  def addPerson(person: Person): Future[Int] = db.run((persons returning persons.map(_.id)) += person)

  def addTask(task: Task): Future[Int] = db.run((tasks returning tasks.map(_.id)) += task)

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
}