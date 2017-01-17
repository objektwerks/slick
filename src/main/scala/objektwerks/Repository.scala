package objektwerks

import slick.jdbc.H2Profile.api._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class Repository(db: Database, awaitDuration: Duration) extends Schema {
  def await[T](future: Future[T]): T = Await.result(future, awaitDuration)

  def createSchema(): Future[Unit] = db.run(DBIO.seq(schema.create))

  def dropSchema(): Future[Unit] = db.run(DBIO.seq(schema.drop))

  def closeDatabase(): Unit = db.close()

  def addRole(role: Role): Future[Int] = db.run(roles.insertOrUpdate(role))

  def addContractorSupplier(contractorSupplier: ContractorSupplier) = db.run(contractorsSuppliers.insertOrUpdate(contractorSupplier))

  def saveCustomer(customer: Customer): Future[Int] = if (customer.id.isEmpty) db.run((customers returning customers.map(_.id)) += customer) else db.run(customers.insertOrUpdate(customer))

  def saveContractor(contractor: Contractor): Future[Int] = if (contractor.id.isEmpty) db.run((contractors returning contractors.map(_.id)) += contractor) else db.run(contractors.insertOrUpdate(contractor))

  def saveTask(task: Task): Future[Int] = if (task.id.isEmpty) db.run((tasks returning tasks.map(_.id)) += task) else db.run(tasks.insertOrUpdate(task))

  def saveSupplier(supplier: Supplier): Future[Int] = if (supplier.id.isEmpty) db.run((suppliers returning suppliers.map(_.id)) += supplier) else db.run(suppliers.insertOrUpdate(supplier))

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

  def listContractors(customer: Customer): Future[Seq[Contractor]] = db.run(contractors.filter(_.id === customer.id).sortBy(_.name.asc).result)

  def listTasks(contractor: Contractor): Future[Seq[Task]] = db.run(tasks.filter(_.id === contractor.id).sortBy(_.started.asc).result)

  def listCustomersContractors(): Future[Seq[(String, String)]] = {
    val query = for {
      c <- customers
      t <- contractors if c.id === t.customerId
    } yield (c.name, t.name)
    db.run(query.result)
  }

  def listContractorsTasks(): Future[Seq[(String, String)]] = {
    val query = for {
      c <- contractors
      t <- tasks if c.id === t.contractorId
    } yield (c.name, t.task)
    db.run(query.result)
  }

  def listContractorsSuppliers(): Future[Seq[(String, String)]] = {
    val query = for {
      c <- contractors
      s <- suppliers
      cs <- contractorsSuppliers if c.id === cs.contractorId && s.id === cs.supplierId
    } yield (c.name, s.name)
    db.run(query.result)
  }
}