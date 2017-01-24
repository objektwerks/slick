package objektwerks

import java.time.LocalDateTime

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.duration._

class RepositoryTest extends FunSuite with BeforeAndAfterAll with Matchers {
  val config = DatabaseConfig.forConfig[H2Profile]("test", ConfigFactory.load("test.conf"))
  val repository = new Repository(config.db, 1 second)
  import repository._

  override protected def beforeAll(): Unit = {
    schema.createStatements foreach println
    schema.dropStatements foreach println
    createSchema()
  }

  override protected def afterAll(): Unit = {
    dropSchema()
    closeDatabase()
  }

  test("add > save") {
    val georgeCustomerId = await(saveCustomer(Customer("george", "1 Mount Vernon., Mount Vernon, VA 22121", "17037802000", "gw@gov.com")))
    val johnCustomerId = await(saveCustomer(Customer("john", "1 Farm Rd., Penn Hill, MA 02169", "16177701175", "ja@gov.com")))
    georgeCustomerId shouldBe 1
    johnCustomerId shouldBe 2

    val poolBoy = Role("pool boy")
    val yardBoy = Role("yard boy")
    await(addRole(poolBoy))
    await(addRole(yardBoy))

    val barneyContractorId = await(saveContractor(Contractor("barney", poolBoy.role, georgeCustomerId)))
    val fredContractorId = await(saveContractor(Contractor("fred", yardBoy.role, johnCustomerId)))
    barneyContractorId shouldBe 1
    fredContractorId shouldBe 2

    val barneyTaskId = await(saveTask(Task(task = "clean pool", recurrence = Recurrence.weekly, contractorId = barneyContractorId)))
    val fredTaskId = await(saveTask(Task(task = "mow yard", recurrence = Recurrence.weekly, contractorId = fredContractorId)))
    barneyTaskId shouldBe 1
    fredTaskId shouldBe 2

    val homeDepotId = await(saveSupplier(Supplier("homedepot", "1 Home Depot Way, Placida, FL 33949", "19413456789", "hd@hd.com")))
    val lowesId = await(saveSupplier(Supplier("lowe", "1 Lowes Way, Placida, FL 33949", "19419874321", "lw@lw.com")))
    await(addContractorSupplier(ContractorSupplier(barneyContractorId, homeDepotId)))
    await(addContractorSupplier(ContractorSupplier(fredContractorId, lowesId)))
  }

  test("find > save") {
    val george = await(findCustomer("george")).get
    val john = await(findCustomer("john")).get
    george.id shouldBe 1
    john.id shouldBe 2

    await(saveCustomer(george.copy(name = "george washington")))
    await(saveCustomer(john.copy(name = "john adams")))

    val barney = await(findContractor("barney")).get
    val fred = await(findContractor("fred")).get
    barney.id shouldBe 1
    fred.id shouldBe 2

    await(saveContractor(barney.copy(name = "barney rebel")))
    await(saveContractor(fred.copy(name = "fred flintstone")))

    val homeDepot = await(findSupplier("homedepot")).get
    val lowes = await(findSupplier("lowe")).get
    homeDepot.id shouldBe 1
    lowes.id shouldBe 2

    await(saveSupplier(homeDepot.copy(name = "home depot")))
    await(saveSupplier(lowes.copy(name = "lowes")))
  }

  test("list") {
    val customers = await(listCustomers())
    customers.size shouldBe 2

    val roles = await(listRoles())
    roles.size shouldBe 2

    customers foreach { customer =>
      val contractors = await(listContractors(customer.id))
      contractors.size shouldBe 1
      contractors foreach { contractor =>
        val tasks = await(listTasks(contractor.id))
        tasks.size shouldBe 1
        tasks foreach { task =>
          val completedTask = task.copy(completed = LocalDateTime.now)
          await(saveTask(completedTask))
        }
      }
    }

    val customersContractors = await(listCustomersContractors())
    customersContractors.size shouldBe 2
    customersContractors foreach println

    val contractorsTasks = await(listContractorsTasks())
    contractorsTasks.size shouldBe 2
    contractorsTasks foreach println

    val contractorsSuppliers = await(listContractorsSuppliers())
    contractorsSuppliers.size shouldBe 2
    contractorsSuppliers foreach println
  }
}