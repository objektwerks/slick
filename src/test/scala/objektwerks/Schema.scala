package objektwerks

import java.sql.Timestamp
import java.time.LocalDateTime

import Recurrence.Recurrence
import slick.jdbc.H2Profile.api._

object Recurrence extends Enumeration {
  type Recurrence = Value
  val once, weekly, biweekly, monthly, quarterly, semiannual, annual = Value
}

/*
  Worker 1 ---> * Task
  Worker 1 ---> 1 Role
  Task 1 ---> 1 Recurrence
 */
trait Schema {
  implicit val dateTimeMapper = MappedColumnType.base[LocalDateTime, Timestamp](l => Timestamp.valueOf(l), t => t.toLocalDateTime)
  implicit val recurrenceMapper = MappedColumnType.base[Recurrence, String](r => r.toString, s => Recurrence.withName(s))
  val roles = TableQuery[Roles]
  val workers = TableQuery[Workers]
  val tasks = TableQuery[Tasks]
  val schema = roles.schema ++ workers.schema ++ tasks.schema

  case class Role(role: String)

  class Roles(tag: Tag) extends Table[Role](tag, "roles") {
    def role = column[String]("role", O.PrimaryKey)
    def * = role <> (Role.apply, Role.unapply)
  }

  case class Worker(id: Option[Int] = None, name: String, role: String)

  class Workers(tag: Tag) extends Table[Worker](tag, "workers") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Unique)
    def role = column[String]("role")
    def * = (id.?, name, role) <> (Worker.tupled, Worker.unapply)
    def roleFk = foreignKey("role_fk", role, TableQuery[Roles])(_.role)
  }

  case class Task(id: Option[Int] = None, workerId: Int, task: String, recurrence: Recurrence, started: LocalDateTime = LocalDateTime.now, completed: Option[LocalDateTime] = None)

  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def workerId = column[Int]("worker_id")
    def task = column[String]("task")
    def recurrence = column[Recurrence]("recurrence")
    def started = column[LocalDateTime]("started")
    def completed = column[Option[LocalDateTime]]("completed")
    def * = (id.?, workerId, task, recurrence, started, completed) <> (Task.tupled, Task.unapply)
    def workerFk = foreignKey("worker_fk", workerId, TableQuery[Workers])(_.id)
  }
}