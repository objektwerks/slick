package objektwerks

import java.sql.Timestamp
import java.time.LocalDateTime

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

object Repository {
  def apply(config: DatabaseConfig[JdbcProfile],
            profile: JdbcProfile,
            awaitDuration: Duration): Repository = new Repository(config, profile, awaitDuration)
}

/**
  * Customer 1 ---> * Contractor 1 ---> * Task
  * Contractor 1 ---> 1 Role
  * Task 1 ---> 1 Recurrence
  * Contractor * <---> * Supplier
  */
class Repository(val config: DatabaseConfig[JdbcProfile],
                 val profile: JdbcProfile,
                 val awaitDuration: Duration) {
  import profile.api._
  import Recurrence._

  implicit val dateTimeMapper = MappedColumnType.base[LocalDateTime, Timestamp](ldt => Timestamp.valueOf(ldt), ts => ts.toLocalDateTime)
  implicit val recurrenceMapper = MappedColumnType.base[Recurrence, String](r => r.toString, s => Recurrence.withName(s))
  val schema = customers.schema ++ roles.schema ++ contractors.schema ++ tasks.schema ++ suppliers.schema ++ contractorsSuppliers.schema
  val db = config.db

  def await[T](action: DBIO[T]): T = Await.result(db.run(action), awaitDuration)
  def exec[T](action: DBIO[T]): Future[T] = db.run(action)
  def close() = db.close()
  def createSchema() = await(DBIO.seq(schema.create))
  def dropSchema() = await(DBIO.seq(schema.drop))

  class Customers(tag: Tag) extends Table[Customer](tag, "customers") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def address = column[String]("address")
    def phone = column[String]("phone")
    def email = column[String]("email")
    def * = (id, name, address, phone, email).<>(Customer.tupled, Customer.unapply)
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
    def save(customer: Customer) = (this returning this.map(_.id)).insertOrUpdate(customer)
    def find(name: String) = compiledFind(name).result.headOption
    def list() = compiledList.result
    def listCustomersContractors() = compiledListCustomersContractors.result
  }

  class Roles(tag: Tag) extends Table[Role](tag, "roles") {
    def name = column[String]("name", O.PrimaryKey)
    def * = name.<>(Role.apply, Role.unapply)
  }
  object roles extends TableQuery(new Roles(_)) {
    val compiledList = Compiled { map(_.name).sortBy(_.asc) }
    def add(role: Role) = this += role
    def list() = compiledList.result
  }

  class Contractors(tag: Tag) extends Table[Contractor](tag, "contractors") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def customerId = column[Int]("customer_id")
    def name = column[String]("name", O.Unique)
    def role = column[String]("role")
    def * = (id, customerId, name, role).<>(Contractor.tupled, Contractor.unapply)
    def roleFk = foreignKey("role_fk", role, TableQuery[Roles])(_.name)
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
    def save(contractor: Contractor) = (this returning this.map(_.id)).insertOrUpdate(contractor)
    def find(name: String) = compiledFind(name).result.headOption
    def list(customerId: Int) = compiledList(customerId).result
    def listContractorsTasks() = compiledListContractorsTasks.result
  }

  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def contractorId = column[Int]("contractor_id")
    def task = column[String]("task")
    def recurrence = column[Recurrence]("recurrence")
    def started = column[LocalDateTime]("started")
    def completed = column[LocalDateTime]("completed")
    def * = (id, contractorId, task, recurrence, started, completed).<>(Task.tupled, Task.unapply)
    def contractorFk = foreignKey("contractor_fk", contractorId, TableQuery[Contractors])(_.id)
  }
  object tasks extends TableQuery(new Tasks(_)) {
    val compiledList = Compiled { contractorId: Rep[Int] => filter(_.id === contractorId).sortBy(_.started.asc) }
    def save(task: Task) = (this returning this.map(_.id)).insertOrUpdate(task)
    def list(contractorId: Int) = compiledList(contractorId).result
  }

  class Suppliers(tag: Tag) extends Table[Supplier](tag, "suppliers") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def address = column[String]("address")
    def phone = column[String]("phone")
    def email = column[String]("email")
    def * = (id, name, address, phone, email).<>(Supplier.tupled, Supplier.unapply)
  }
  object suppliers extends TableQuery(new Suppliers(_)) {
    val compiledFind = Compiled { name: Rep[String] => filter(_.name === name) }
    def save(supplier: Supplier) = (this returning this.map(_.id)).insertOrUpdate(supplier)
    def find(name: String) = compiledFind(name).result.headOption
  }

  class ContractorsSuppliers(tag: Tag) extends Table[ContractorSupplier](tag, "contractors_suppliers") {
    def contractorId = column[Int]("contractor_id")
    def supplierId = column[Int]("supplier_id")
    def * = (contractorId, supplierId).<>(ContractorSupplier.tupled, ContractorSupplier.unapply)
    def pk = primaryKey("pk", (contractorId, supplierId))
    def contractorFk = foreignKey("contractor_supplier_fk", contractorId, TableQuery[Contractors])(_.id)
    def supplierFk = foreignKey("supplier_contractor_fk", supplierId, TableQuery[Suppliers])(_.id)
  }
  object contractorsSuppliers extends TableQuery(new ContractorsSuppliers(_)) {
    val compiledListContractorsSuppliers = Compiled {
      for {
        cs <- this
        c  <- contractors
        s  <- suppliers
        if c.id === cs.contractorId && s.id === cs.supplierId
      } yield (c.name, s.name)
    }
    def add(contractorSupplier: ContractorSupplier) = this += contractorSupplier
    def listContractorsSuppliers() = compiledListContractorsSuppliers.result
  }
}