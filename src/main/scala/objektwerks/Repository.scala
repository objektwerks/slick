package objektwerks

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Mode, OutputTimeUnit}
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class Repository(db: Database, awaitDuration: Duration) extends Schema {
  @Benchmark
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

  def findCustomer(name: String): Future[Option[Customer]] = db.run(customers.filter(_.name === name).result.headOption)

  def findContractor(name: String): Future[Option[Contractor]] = db.run(contractors.filter(_.name === name).result.headOption)

  def findSupplier(name: String): Future[Option[Supplier]] = db.run(suppliers.filter(_.name === name).result.headOption)

  def listRoles(): Future[Seq[String]] = db.run(roles.map(_.role).sortBy(_.asc).result)

  def listCustomers(): Future[Seq[Customer]] = db.run(customers.sortBy(_.name.asc).result)

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