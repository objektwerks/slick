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
  Contractor 1 ---> * Task
  Contractor 1 ---> 1 Role
  Task 1 ---> 1 Recurrence
  Contractor * <---> * Customer
 */
trait Schema {
  implicit val dateTimeMapper = MappedColumnType.base[LocalDateTime, Timestamp](l => Timestamp.valueOf(l), t => t.toLocalDateTime)
  implicit val recurrenceMapper = MappedColumnType.base[Recurrence, String](r => r.toString, s => Recurrence.withName(s))
  val roles = TableQuery[Roles]
  val contractors = TableQuery[Contractors]
  val tasks = TableQuery[Tasks]
  val schema = roles.schema ++ contractors.schema ++ tasks.schema

  case class Role(role: String)

  class Roles(tag: Tag) extends Table[Role](tag, "roles") {
    def role = column[String]("role", O.PrimaryKey)
    def * = role <> (Role.apply, Role.unapply)
  }

  case class Contractor(id: Option[Int] = None, name: String, role: String)

  class Contractors(tag: Tag) extends Table[Contractor](tag, "contractors") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Unique)
    def role = column[String]("role")
    def * = (id.?, name, role) <> (Contractor.tupled, Contractor.unapply)
    def roleFk = foreignKey("role_fk", role, TableQuery[Roles])(_.role)
  }

  case class Task(id: Option[Int] = None, contractorId: Int, task: String, recurrence: Recurrence, started: LocalDateTime = LocalDateTime.now, completed: Option[LocalDateTime] = None)

  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def contractorId = column[Int]("contractor_id")
    def task = column[String]("task")
    def recurrence = column[Recurrence]("recurrence")
    def started = column[LocalDateTime]("started")
    def completed = column[Option[LocalDateTime]]("completed")
    def * = (id.?, contractorId, task, recurrence, started, completed) <> (Task.tupled, Task.unapply)
    def contractorFk = foreignKey("contractor_fk", contractorId, TableQuery[Contractors])(_.id)
  }
}