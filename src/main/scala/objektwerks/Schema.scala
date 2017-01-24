package objektwerks

import java.sql.Timestamp
import java.time.LocalDateTime

import objektwerks.Recurrence.Recurrence
import slick.jdbc.H2Profile.api._

object Recurrence extends Enumeration {
  type Recurrence = Value
  val once, weekly, biweekly, monthly, quarterly, semiannual, annual = Value
}

/**
 * Customer 1 ---> * Contractor 1 ---> * Task
 * Contractor 1 ---> 1 Role
 * Task 1 ---> 1 Recurrence
 * Contractor * <---> * Supplier
 */
trait Schema {
  implicit val dateTimeMapper = MappedColumnType.base[LocalDateTime, Timestamp](l => Timestamp.valueOf(l), t => t.toLocalDateTime)
  implicit val recurrenceMapper = MappedColumnType.base[Recurrence, String](r => r.toString, s => Recurrence.withName(s))

  case class Customer(name: String, address: String, phone: String, email: String, id: Int = 0)
  class Customers(tag: Tag) extends Table[Customer](tag, "customers") {
    def name = column[String]("name")
    def address = column[String]("address")
    def phone = column[String]("phone")
    def email = column[String]("email")
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def * = (name, address, phone, email, id) <> (Customer.tupled, Customer.unapply)
  }

  case class Role(role: String)
  class Roles(tag: Tag) extends Table[Role](tag, "roles") {
    def role = column[String]("role", O.PrimaryKey)
    def * = role <> (Role.apply, Role.unapply)
  }

  case class Contractor(name: String, role: String, customerId: Int, id: Int = 0)
  class Contractors(tag: Tag) extends Table[Contractor](tag, "contractors") {
    def name = column[String]("name", O.Unique)
    def role = column[String]("role")
    def customerId = column[Int]("customer_id")
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def * = (name, role, customerId, id) <> (Contractor.tupled, Contractor.unapply)
    def roleFk = foreignKey("role_fk", role, TableQuery[Roles])(_.role)
    def customerFk = foreignKey("customer_fk", customerId, TableQuery[Customers])(_.id)
  }

  case class Task(task: String, recurrence: Recurrence, started: LocalDateTime = LocalDateTime.now, completed: LocalDateTime = LocalDateTime.now, contractorId: Int, id: Int = 0)
  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def task = column[String]("task")
    def recurrence = column[Recurrence]("recurrence")
    def started = column[LocalDateTime]("started")
    def completed = column[LocalDateTime]("completed")
    def contractorId = column[Int]("contractor_id")
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def * = (task, recurrence, started, completed, contractorId, id) <> (Task.tupled, Task.unapply)
    def contractorFk = foreignKey("contractor_fk", contractorId, TableQuery[Contractors])(_.id)
  }

  case class Supplier(name: String, address: String, phone: String, email: String, id: Int = 0)
  class Suppliers(tag: Tag) extends Table[Supplier](tag, "suppliers") {
    def name = column[String]("name")
    def address = column[String]("address")
    def phone = column[String]("phone")
    def email = column[String]("email")
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def * = (name, address, phone, email, id) <> (Supplier.tupled, Supplier.unapply)
  }

  case class ContractorSupplier(contractorId: Int, supplierId: Int)
  class ContractorsSuppliers(tag: Tag) extends Table[ContractorSupplier](tag, "contractors_suppliers") {
    def contractorId = column[Int]("contractor_id")
    def supplierId = column[Int]("supplier_id")
    def * = (contractorId, supplierId) <> (ContractorSupplier.tupled, ContractorSupplier.unapply)
    def pk = primaryKey("pk", (contractorId, supplierId))
    def contractorFk = foreignKey("contractor_supplier_fk", contractorId, TableQuery[Contractors])(_.id)
    def supplierFk = foreignKey("supplier_contractor_fk", supplierId, TableQuery[Suppliers])(_.id)
  }
}