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
  val customers = TableQuery[Customers]
  val customersContractors = TableQuery[CustomersContractors]
  val schema = roles.schema ++ contractors.schema ++ tasks.schema ++ customers.schema ++ customersContractors.schema

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

  case class Customer(id: Option[Int] = None, name: String, address: String, phone: Int)

  class Customers(tag: Tag) extends Table[Customer](tag, "customers") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def address = column[String]("address")
    def phone = column[Int]("phone")
    def * = (id.?, name, address, phone) <> (Customer.tupled, Customer.unapply)
  }

  case class CustomerContractor(customerId: Int, contractorId: Int)

  class CustomersContractors(tag: Tag) extends Table[CustomerContractor](tag, "customers_contractors") {
    def customerId = column[Int]("customer_id")
    def contractorId = column[Int]("contractor_id")
    def * = (customerId, contractorId) <> (CustomerContractor.tupled, CustomerContractor.unapply)
    def customerFk = foreignKey("customer_contractor_fk", customerId, TableQuery[Customers])(_.id, onDelete = ForeignKeyAction.Cascade)
    def contractorFk = foreignKey("contractor_customer_fk", contractorId, TableQuery[Contractors])(_.id, onDelete = ForeignKeyAction.Cascade)
  }
}