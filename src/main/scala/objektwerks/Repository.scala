package objektwerks

import slick.jdbc.H2Profile.api._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class Repository(db: Database, awaitDuration: Duration) extends Schema {
  val schema = customers.schema ++ roles.schema ++ contractors.schema ++ tasks.schema ++ suppliers.schema ++ contractorsSuppliers.schema

  def await[T](future: Future[T]): T = Await.result(future, awaitDuration)

  def exec[T](action: DBIO[T]): T = Await.result(db.run(action), awaitDuration)

  def createSchema() = exec(DBIO.seq(schema.create))

  def dropSchema() = exec(DBIO.seq(schema.drop))

  def closeDatabase() = db.close()

  def addRole(role: Role): Future[Int] = db.run(roles += role)

  def addContractorSupplier(contractorSupplier: ContractorSupplier): Future[Int] = db.run(contractorsSuppliers += contractorSupplier)

  def saveCustomer(customer: Customer): Future[Int] = if (customer.id == 0) db.run((customers returning customers.map(_.id)) += customer) else db.run(customers.insertOrUpdate(customer))

  def saveContractor(contractor: Contractor): Future[Int] = if (contractor.id == 0) db.run((contractors returning contractors.map(_.id)) += contractor) else db.run(contractors.insertOrUpdate(contractor))

  def saveTask(task: Task): Future[Int] = if (task.id == 0) db.run((tasks returning tasks.map(_.id)) += task) else db.run(tasks.insertOrUpdate(task))

  def saveSupplier(supplier: Supplier): Future[Int] = if (supplier.id == 0) db.run((suppliers returning suppliers.map(_.id)) += supplier) else db.run(suppliers.insertOrUpdate(supplier))

  val compiledFindCustomer = Compiled { name: Rep[String] => customers.filter(_.name === name) }
  def findCustomer(name: String): Future[Option[Customer]] = db.run(compiledFindCustomer(name).result.headOption)

  val compiledFindContractor = Compiled { name: Rep[String] => contractors.filter(_.name === name) }
  def findContractor(name: String): Future[Option[Contractor]] = db.run(compiledFindContractor(name).result.headOption)

  val compiledFindSupplier = Compiled { name: Rep[String] => suppliers.filter(_.name === name) }
  def findSupplier(name: String): Future[Option[Supplier]] = db.run(compiledFindSupplier(name).result.headOption)

  val compiledListRoles = Compiled { roles.map(_.role).sortBy(_.asc) }
  def listRoles(): Future[Seq[String]] = db.run(compiledListRoles.result)

  val compiledListCustomers = Compiled { customers.sortBy(_.name.asc) }
  def listCustomers(): Future[Seq[Customer]] = db.run(compiledListCustomers.result)

  val compiledListContractors = Compiled { customerId: Rep[Int] => contractors.filter(_.id === customerId).sortBy(_.name.asc) }
  def listContractors(customerId: Int): Future[Seq[Contractor]] = db.run(compiledListContractors(customerId).result)

  val compiledListTasks = Compiled { contractorId: Rep[Int] => tasks.filter(_.id === contractorId).sortBy(_.started.asc) }
  def listTasks(contractorId: Int): Future[Seq[Task]] = db.run(compiledListTasks(contractorId).result)

  val compiledListCustomersContractors = Compiled {
    for {
      c <- customers
      t <- contractors if c.id === t.customerId
    } yield (c.name, t.name)
  }
  def listCustomersContractors(): Future[Seq[(String, String)]] = db.run(compiledListCustomersContractors.result)

  val compiledListContractorsTasks = Compiled {
    for {
      c <- contractors
      t <- tasks if c.id === t.contractorId
    } yield (c.name, t.task)
  }
  def listContractorsTasks(): Future[Seq[(String, String)]] = db.run(compiledListContractorsTasks.result)

  val compiledListContractorsSuppliers = Compiled {
    for {
      c <- contractors
      s <- suppliers
      cs <- contractorsSuppliers if c.id === cs.contractorId && s.id === cs.supplierId
    } yield (c.name, s.name)
  }
  def listContractorsSuppliers(): Future[Seq[(String, String)]] = db.run(compiledListContractorsSuppliers.result)
}