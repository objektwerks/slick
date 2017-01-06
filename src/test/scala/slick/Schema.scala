package slick

import java.sql.Timestamp
import java.time.LocalDateTime

import slick.jdbc.H2Profile.api._

trait Schema {
  implicit val LocalDateTimeMapper = MappedColumnType.base[LocalDateTime, Timestamp](l => Timestamp.valueOf(l), t => t.toLocalDateTime)
  val workers = TableQuery[Workers]
  val tasks = TableQuery[Tasks]
  val schema = workers.schema ++ tasks.schema

  case class Worker(id: Option[Int] = None, name: String)

  class Workers(tag: Tag) extends Table[Worker](tag, "workers") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Unique)
    def * = (id.?, name) <> (Worker.tupled, Worker.unapply)
  }

  case class Task(id: Option[Int] = None, personId: Int, task: String, assigned: LocalDateTime = LocalDateTime.now, completed: Option[LocalDateTime] = None)

  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def workerId = column[Int]("worker_id")
    def task = column[String]("task")
    def assigned = column[LocalDateTime]("assigned")
    def completed = column[Option[LocalDateTime]]("completed")
    def * = (id.?, workerId, task, assigned, completed) <> (Task.tupled, Task.unapply)
    def workerFk = foreignKey("worker_fk", workerId, TableQuery[Workers])(_.id)
  }
}