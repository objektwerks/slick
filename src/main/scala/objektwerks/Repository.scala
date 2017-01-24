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