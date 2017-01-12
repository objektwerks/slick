package objektwerks

import java.sql.Timestamp
import java.time.LocalDateTime

import Recurrence.Recurrence
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
  val customers = TableQuery[Customers]
  val roles = TableQuery[Roles]
  val contractors = TableQuery[Contractors]
  val tasks = TableQuery[Tasks]
  val suppliers = TableQuery[Suppliers]
  val contractorsSuppliers = TableQuery[ContractorsSuppliers]
  val schema = customers.schema ++ roles.schema ++ contractors.schema ++ tasks.schema ++ suppliers.schema ++ contractorsSuppliers.schema

  case class Customer(id: Option[Int] = None, name: String, address: String, phone: String, email: String)

  class Customers(tag: Tag) extends Table[Customer](tag, "customers") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def address = column[String]("address")
    def phone = column[String]("phone")
    def email = column[String]("email")
    def * = (id.?, name, address, phone, email) <> (Customer.tupled, Customer.unapply)
  }

  case class Role(role: String)

  class Roles(tag: Tag) extends Table[Role](tag, "roles") {
    def role = column[String]("role", O.PrimaryKey)
    def * = role <> (Role.apply, Role.unapply)
  }

  case class Contractor(id: Option[Int] = None, customerId: Int, name: String, role: String)

  class Contractors(tag: Tag) extends Table[Contractor](tag, "contractors") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def customerId = column[Int]("customer_id")
    def name = column[String]("name", O.Unique)
    def role = column[String]("role")
    def * = (id.?, customerId, name, role) <> (Contractor.tupled, Contractor.unapply)
    def roleFk = foreignKey("role_fk", role, TableQuery[Roles])(_.role)
    def customerFk = foreignKey("customer_fk", customerId, TableQuery[Customers])(_.id)
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

  case class Supplier(id: Option[Int] = None, name: String, address: String, phone: String, email: String)

  class Suppliers(tag: Tag) extends Table[Supplier](tag, "suppliers") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def address = column[String]("address")
    def phone = column[String]("phone")
    def email = column[String]("email")
    def * = (id.?, name, address, phone, email) <> (Supplier.tupled, Supplier.unapply)
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