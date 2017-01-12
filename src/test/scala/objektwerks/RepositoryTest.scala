package objektwerks

import java.time.LocalDateTime

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

class RepositoryTest extends FunSuite with BeforeAndAfterAll with Matchers {
  val config = DatabaseConfig.forConfig[H2Profile]("test", ConfigFactory.load("test.conf"))
  val repository = new Repository(config.db)
  import repository._

  override protected def beforeAll(): Unit = {
    schema.createStatements foreach println
    schema.dropStatements foreach println
    await(createSchema())
  }

  override protected def afterAll(): Unit = {
    await(dropSchema())
    closeDatabase()
  }

  test("add > save") {
    val georgeCustomerId = await(saveCustomer(Customer(name = "george", address = "1 Mount Vernon., Mount Vernon, VA 22121", phone = "17037802000", email = "gw@gov.com")))
    val johnCustomerId = await(saveCustomer(Customer(name = "john", address = "1 Farm Rd., Penn Hill, MA 02169", phone = "16177701175", email = "ja@gov.com")))
    georgeCustomerId shouldBe 1
    johnCustomerId shouldBe 2

    val poolBoy = Role("pool boy")
    val yardBoy = Role("yard boy")
    await(addRole(poolBoy))
    await(addRole(yardBoy))

    val barneyContractorId = await(saveContractor(Contractor(customerId = georgeCustomerId, name = "barney", role = poolBoy.role)))
    val fredContractorId = await(saveContractor(Contractor(customerId = johnCustomerId, name = "fred", role = yardBoy.role)))
    barneyContractorId shouldBe 1
    fredContractorId shouldBe 2

    val barneyTaskId = await(saveTask(Task(contractorId = barneyContractorId, task = "clean pool", recurrence = Recurrence.weekly, completed = Some(LocalDateTime.now))))
    val fredTaskId = await(saveTask(Task(contractorId = fredContractorId, task = "mow yard", recurrence = Recurrence.weekly, completed = Some(LocalDateTime.now))))
    barneyTaskId shouldBe 1
    fredTaskId shouldBe 2

    val homeDepotId = await(saveSupplier(Supplier(name = "homedepot", address = "1 Home Depot Way, Placida, FL 33949", phone = "19413456789", email = "hd@hd.com")))
    val lowesId = await(saveSupplier(Supplier(name = "lowe", address = "1 Lowes Way, Placida, FL 33949", phone = "19419874321", email = "lw@lw.com")))
    await(addContractorSupplier(ContractorSupplier(barneyContractorId, homeDepotId)))
    await(addContractorSupplier(ContractorSupplier(fredContractorId, lowesId)))
  }

  test("find > save") {
    val george = await(findCustomer("george"))
    val john = await(findCustomer("john"))
    george.id shouldBe Some(1)
    john.id shouldBe Some(2)

    await(saveCustomer(george.copy(name = "george washington")))
    await(saveCustomer(john.copy(name = "john adams")))

    val barney = await(findContractor("barney"))
    val fred = await(findContractor("fred"))
    barney.id shouldBe Some(1)
    fred.id shouldBe Some(2)

    await(saveContractor(barney.copy(name = "barney rebel")))
    await(saveContractor(fred.copy(name = "fred flintstone")))

    val homeDepot = await(findSupplier("homedepot"))
    val lowes = await(findSupplier("lowe"))
    homeDepot.id shouldBe Some(1)
    lowes.id shouldBe Some(2)

    await(saveSupplier(homeDepot.copy(name = "home depot")))
    await(saveSupplier(lowes.copy(name = "lowes")))
  }

  test("list") {
    val customers = await(listCustomers())
    customers.size shouldBe 2

    val roles = await(listRoles())
    roles.size shouldBe 2

    val contractors = await(listContractors())
    contractors.size shouldBe 2
    contractors foreach { c =>
      val tasks = await(listTasks(c))
      tasks.size shouldBe 1
      tasks foreach { t =>
        val completedTask = t.copy(completed = Some(LocalDateTime.now))
        await(saveTask(completedTask))
      }
    }

    val suppliers = await(listSuppliers())
    suppliers.size shouldBe 2

    val customersContractors = await(listCustomersContractors())
    customersContractors.size shouldBe 2
    customersContractors foreach println

    val contractorsTasks = await(listContractorsTasks())
    contractorsTasks.size shouldBe 2
    contractorsTasks foreach println
  }
}