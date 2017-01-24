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

  val compiledFindContractor = Compiled { name: Rep[String] => contractors.filter(_.name === name) }
  def findContractor(name: String): Future[Option[Contractor]] = db.run(compiledFindContractor(name).result.headOption)

  val compiledFindSupplier = Compiled { name: Rep[String] => suppliers.filter(_.name === name) }
  def findSupplier(name: String): Future[Option[Supplier]] = db.run(compiledFindSupplier(name).result.headOption)

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