package objektwerks

import java.sql.Timestamp
import java.time.LocalDateTime

import objektwerks.Recurrence.Recurrence
import slick.jdbc.H2Profile.api._

object Recurrence extends Enumeration {
  type Recurrence = Value
  val once, weekly, biweekly, monthly, quarterly, semiannual, annual = Value
  implicit val recurrenceMapper = MappedColumnType.base[Recurrence, String](r => r.toString, s => Recurrence.withName(s))
}

/**
 * Customer 1 ---> * Contractor 1 ---> * Task
 * Contractor 1 ---> 1 Role
 * Task 1 ---> 1 Recurrence
 * Contractor * <---> * Supplier
 */
trait Schema {
  implicit val dateTimeMapper = MappedColumnType.base[LocalDateTime, Timestamp](l => Timestamp.valueOf(l), t => t.toLocalDateTime)
  val schema = customers.schema ++ roles.schema ++ contractors.schema ++ tasks.schema ++ suppliers.schema ++ contractorsSuppliers.schema

  def createSchema() = DBIO.seq(schema.create)

  def dropSchema() = DBIO.seq(schema.drop)

  case class Customer(name: String, address: String, phone: String, email: String, id: Int = 0)
  class Customers(tag: Tag) extends Table[Customer](tag, "customers") {
    def name = column[String]("name")
    def address = column[String]("address")
    def phone = column[String]("phone")
    def email = column[String]("email")
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def * = (name, address, phone, email, id) <> (Customer.tupled, Customer.unapply)
  }
  object customers extends TableQuery(new Customers(_)) {
    val compiledFind = Compiled { name: Rep[String] => filter(_.name === name) }
    val compiledList = Compiled { sortBy(_.name.asc) }
    val compiledListCustomersContractors = Compiled {
      for {
        c <- this
        t <- contractors if c.id === t.customerId
      } yield (c.name, t.name)
    }
    def save(customer: Customer) = if (customer.id == 0) (this returning this.map(_.id)) += customer else this.insertOrUpdate(customer)
    def find(name: String) = compiledFind(name).result.headOption
    def list() = compiledList.result
    def listCustomersContractors() = compiledListCustomersContractors.result
  }

  case class Role(role: String)
  class Roles(tag: Tag) extends Table[Role](tag, "roles") {
    def role = column[String]("role", O.PrimaryKey)
    def * = role <> (Role.apply, Role.unapply)
  }
  object roles extends TableQuery(new Roles(_)) {
    val compiledList = Compiled { map(_.role).sortBy(_.asc) }
    def add(role: Role) = this += role
    def list() = compiledList.result
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
  object contractors extends TableQuery(new Contractors(_)) {
    val compiledFind = Compiled { name: Rep[String] => filter(_.name === name) }
    val compiledList = Compiled { customerId: Rep[Int] => filter(_.id === customerId).sortBy(_.name.asc) }
    val compiledListContractorsTasks = Compiled {
      for {
        c <- this
        t <- tasks if c.id === t.contractorId
      } yield (c.name, t.task)
    }
    def save(contractor: Contractor) = if (contractor.id == 0) (this returning this.map(_.id)) += contractor else this.insertOrUpdate(contractor)
    def find(name: String) = compiledFind(name).result.headOption
    def list(customerId: Int) = compiledList(customerId).result
    def listContractorsTasks() = compiledListContractorsTasks.result
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
  object tasks extends TableQuery(new Tasks(_)) {
    val compiledList = Compiled { contractorId: Rep[Int] => filter(_.id === contractorId).sortBy(_.started.asc) }
    def save(task: Task) = if (task.id == 0) (this returning this.map(_.id)) += task else this.insertOrUpdate(task)
    def list(contractorId: Int) = compiledList(contractorId).result
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
  object suppliers extends TableQuery(new Suppliers(_)) {
    val compiledFind = Compiled { name: Rep[String] => filter(_.name === name) }
    def save(supplier: Supplier) = if (supplier.id == 0) (this returning this.map(_.id)) += supplier else this.insertOrUpdate(supplier)
    def find(name: String) = compiledFind(name).result.headOption
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
  object contractorsSuppliers extends TableQuery(new ContractorsSuppliers(_)) {
    val compiledListContractorsSuppliers = Compiled {
      for {
        c <- contractors
        s <- suppliers
        cs <- this if c.id === cs.contractorId && s.id === cs.supplierId
      } yield (c.name, s.name)
    }
    def add(contractorSupplier: ContractorSupplier) = this += contractorSupplier
    def listContractorsSuppliers() = compiledListContractorsSuppliers.result
  }
}