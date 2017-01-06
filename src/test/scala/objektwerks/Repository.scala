package objektwerks

import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class Repository(db: Database) extends Schema {
  def await[T](future: Future[T], duration: Duration): T = Await.result(future, duration)

  def createSchema(): Future[Unit] = db.run(DBIO.seq(schema.create))

  def dropSchema(): Future[Unit] = db.run(DBIO.seq(schema.drop))

  def closeDatabase(): Unit = db.close()

  def addRole(role: Role): Future[Int] = db.run(roles.insertOrUpdate(role))

  def saveWorker(worker: Worker): Future[Int] = db.run((workers returning workers.map(_.id)) += worker)

  def saveTask(task: Task): Future[Int] = if (task.id.isEmpty) db.run((tasks returning tasks.map(_.id)) += task) else db.run(tasks.insertOrUpdate(task))

  def findWorker(name: String): Future[Worker] = db.run(workers.filter(_.name === name).result.head)

  def listRoles(): Future[Seq[String]] = db.run(roles.map(_.role).sortBy(_.asc).result)

  def listWorkers(): Future[Seq[Worker]] = db.run(workers.sortBy(_.name.asc).result)

  def listTasks(person: Worker): Future[Seq[Task]] = db.run(tasks.filter(_.id === person.id).sortBy(_.assigned.asc).result)

  def listWorkersTasks(): Future[Seq[(String, String)]] = {
    val query = for {
      w <- workers
      t <- tasks if w.id === t.workerId
    } yield (w.name, t.task)
    db.run(query.result)
  }
}