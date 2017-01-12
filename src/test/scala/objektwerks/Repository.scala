package objektwerks

import slick.jdbc.H2Profile.api._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class Repository(db: Database) extends Schema {
  def await[T](future: Future[T]): T = Await.result(future, 1 second)

  def createSchema(): Future[Unit] = db.run(DBIO.seq(schema.create))

  def dropSchema(): Future[Unit] = db.run(DBIO.seq(schema.drop))

  def closeDatabase(): Unit = db.close()

  def addRole(role: Role): Future[Int] = db.run(roles.insertOrUpdate(role))

  def addContractorSupplier(contractorSupplier: ContractorSupplier) = db.run(contractorsSuppliers.insertOrUpdate(contractorSupplier))

  def saveCustomer(customer: Customer): Future[Int] = if (customer.id.isEmpty) db.run((customers returning customers.map(_.id)) += customer) else db.run(customers.insertOrUpdate(customer))

  def saveContractor(contractor: Contractor): Future[Int] = if (contractor.id.isEmpty) db.run((contractors returning contractors.map(_.id)) += contractor) else db.run(contractors.insertOrUpdate(contractor))

  def saveTask(task: Task): Future[Int] = if (task.id.isEmpty) db.run((tasks returning tasks.map(_.id)) += task) else db.run(tasks.insertOrUpdate(task))

  def saveSupplier(supplier: Supplier): Future[Int] = if (supplier.id.isEmpty) db.run((suppliers returning suppliers.map(_.id)) += supplier) else db.run(suppliers.insertOrUpdate(supplier))

  def findContractor(name: String): Future[Contractor] = db.run(contractors.filter(_.name === name).result.head)

  def listRoles(): Future[Seq[String]] = db.run(roles.map(_.role).sortBy(_.asc).result)

  def listContractors(): Future[Seq[Contractor]] = db.run(contractors.sortBy(_.name.asc).result)

  def listTasks(contractor: Contractor): Future[Seq[Task]] = db.run(tasks.filter(_.id === contractor.id).sortBy(_.started.asc).result)

  def listContractorsTasks(): Future[Seq[(String, String)]] = {
    val query = for {
      w <- contractors
      t <- tasks if w.id === t.contractorId
    } yield (w.name, t.task)
    db.run(query.result)
  }
}