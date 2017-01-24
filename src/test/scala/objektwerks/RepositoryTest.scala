package objektwerks

import java.time.LocalDateTime

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import slick.basic.DatabaseConfig
import slick.jdbc.{H2Profile, JdbcProfile}

import scala.concurrent.duration._

class RepositoryTest extends FunSuite with BeforeAndAfterAll with Matchers {
  val config = DatabaseConfig.forConfig[JdbcProfile]("test", ConfigFactory.load("test.conf"))
  val repository = new Repository(config, H2Profile, 1 second)
  import repository._

  override protected def beforeAll(): Unit = {
    schema.createStatements foreach println
    schema.dropStatements foreach println
    createSchema()
  }

  override protected def afterAll(): Unit = {
    dropSchema()
    closeRepository()
  }

  test("add > save") {
    val georgeCustomerId = exec(customers.save(Customer("george", "1 Mount Vernon., Mount Vernon, VA 22121", "17037802000", "gw@gov.com")))
    val johnCustomerId = exec(customers.save(Customer("john", "1 Farm Rd., Penn Hill, MA 02169", "16177701175", "ja@gov.com")))
    georgeCustomerId shouldBe 1
    johnCustomerId shouldBe 2

    val poolBoy = Role("pool boy")
    val yardBoy = Role("yard boy")
    exec(roles.add(poolBoy))
    exec(roles.add(yardBoy))

    val barneyContractorId = exec(contractors.save(Contractor("barney", poolBoy.role, georgeCustomerId)))
    val fredContractorId = exec(contractors.save(Contractor("fred", yardBoy.role, johnCustomerId)))
    barneyContractorId shouldBe 1
    fredContractorId shouldBe 2

    val barneyTaskId = exec(tasks.save(Task(task = "clean pool", recurrence = Recurrence.weekly, contractorId = barneyContractorId)))
    val fredTaskId = exec(tasks.save(Task(task = "mow yard", recurrence = Recurrence.weekly, contractorId = fredContractorId)))
    barneyTaskId shouldBe 1
    fredTaskId shouldBe 2

    val homeDepotId = exec(suppliers.save(Supplier("homedepot", "1 Home Depot Way, Placida, FL 33949", "19413456789", "hd@hd.com")))
    val lowesId = exec(suppliers.save(Supplier("lowe", "1 Lowes Way, Placida, FL 33949", "19419874321", "lw@lw.com")))
    exec(contractorsSuppliers.add(ContractorSupplier(barneyContractorId, homeDepotId)))
    exec(contractorsSuppliers.add(ContractorSupplier(fredContractorId, lowesId)))
  }

  test("find > save") {
    val george = exec(customers.find("george")).get
    val john = exec(customers.find("john")).get
    george.id shouldBe 1
    john.id shouldBe 2

    exec(customers.save(george.copy(name = "george washington")))
    exec(customers.save(john.copy(name = "john adams")))

    val barney = exec(contractors.find("barney")).get
    val fred = exec(contractors.find("fred")).get
    barney.id shouldBe 1
    fred.id shouldBe 2

    exec(contractors.save(barney.copy(name = "barney rebel")))
    exec(contractors.save(fred.copy(name = "fred flintstone")))

    val homeDepot = exec(suppliers.find("homedepot")).get
    val lowes = exec(suppliers.find("lowe")).get
    homeDepot.id shouldBe 1
    lowes.id shouldBe 2

    exec(suppliers.save(homeDepot.copy(name = "home depot")))
    exec(suppliers.save(lowes.copy(name = "lowes")))
  }

  test("list") {
    val customerList = exec(customers.list())
    customerList.size shouldBe 2

    exec(roles.list()).size shouldBe 2

    customerList foreach { customer =>
      val contractorList = exec(contractors.list(customer.id))
      contractorList.size shouldBe 1
      contractorList foreach { contractor =>
        val taskList = exec(tasks.list(contractor.id))
        taskList.size shouldBe 1
        taskList foreach { task =>
          val completedTask = task.copy(completed = LocalDateTime.now)
          exec(tasks.save(completedTask))
        }
      }
    }

    val customersContractors = exec(customers.listCustomersContractors())
    customersContractors.size shouldBe 2
    customersContractors foreach println

    val contractorsTasks = exec(contractors.listContractorsTasks())
    contractorsTasks.size shouldBe 2
    contractorsTasks foreach println

    val contractorsSuppliersList = exec(contractorsSuppliers.listContractorsSuppliers())
    contractorsSuppliersList.size shouldBe 2
    contractorsSuppliersList foreach println
  }
}