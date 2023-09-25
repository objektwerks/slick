package objektwerks

import java.time.LocalDateTime

import com.typesafe.config.ConfigFactory

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import slick.basic.DatabaseConfig
import slick.jdbc.{H2Profile, JdbcProfile}

import scala.concurrent.duration._
import scala.language.postfixOps

final class RepositoryTest extends AnyFunSuite with BeforeAndAfterAll with Matchers:
  val config = DatabaseConfig.forConfig[JdbcProfile]("test", ConfigFactory.load("test.conf"))
  val repository = Repository(config, H2Profile, 1 second)

  override protected def beforeAll(): Unit =
    repository.schema.createStatements foreach println
    repository.schema.dropStatements foreach println
    repository.createSchema()

  override protected def afterAll(): Unit =
    repository.dropSchema()
    repository.close()

  test("add > save"):
    import repository.*

    val georgeCustomerId = await(customers.save(Customer(name = "george", address = "1 Mount Vernon., Mount Vernon, VA 22121", phone = "17037802000", email = "gw@gov.com"))).get
    val johnCustomerId = await(customers.save(Customer(name = "john", address = "1 Farm Rd., Penn Hill, MA 02169", phone = "16177701175", email = "ja@gov.com"))).get
    georgeCustomerId shouldBe 1
    johnCustomerId shouldBe 2

    val poolBoy = Role(name = "pool boy")
    val yardBoy = Role(name = "yard boy")
    await(roles.add(poolBoy))
    await(roles.add(yardBoy))

    val barneyContractorId = await(contractors.save(Contractor(customerId = georgeCustomerId, name = "barney", role = poolBoy.name))).get
    val fredContractorId = await(contractors.save(Contractor(customerId = johnCustomerId, name = "fred", role = yardBoy.name))).get
    barneyContractorId shouldBe 1
    fredContractorId shouldBe 2

    val barneyTaskId = await(tasks.save(Task( contractorId = barneyContractorId, task = "clean pool", recurrence = Recurrence.weekly))).get
    val fredTaskId = await(tasks.save(Task(contractorId = fredContractorId, task = "mow yard", recurrence = Recurrence.weekly))).get
    barneyTaskId shouldBe 1
    fredTaskId shouldBe 2

    val homeDepotId = await(suppliers.save(Supplier(name = "homedepot", address = "1 Home Depot Way, Placida, FL 33949", phone = "19413456789", email = "hd@hd.com"))).get
    val lowesId = await(suppliers.save(Supplier(name = "lowe", address = "1 Lowes Way, Placida, FL 33949", phone = "19419874321", email = "lw@lw.com"))).get
    await(contractorsSuppliers.add(ContractorSupplier(barneyContractorId, homeDepotId)))
    await(contractorsSuppliers.add(ContractorSupplier(fredContractorId, lowesId)))

  test("find > save"):
    import repository.*

    val george = await(customers.find("george")).get
    val john = await(customers.find("john")).get
    george.id shouldBe 1
    john.id shouldBe 2

    await(customers.save(george.copy(name = "george washington")))
    await(customers.save(john.copy(name = "john adams")))

    val barney = await(contractors.find("barney")).get
    val fred = await(contractors.find("fred")).get
    barney.id shouldBe 1
    fred.id shouldBe 2

    await(contractors.save(barney.copy(name = "barney rebel")))
    await(contractors.save(fred.copy(name = "fred flintstone")))

    val homeDepot = await(suppliers.find("homedepot")).get
    val lowes = await(suppliers.find("lowe")).get
    homeDepot.id shouldBe 1
    lowes.id shouldBe 2

    await(suppliers.save(homeDepot.copy(name = "home depot")))
    await(suppliers.save(lowes.copy(name = "lowes")))

  test("list"):
    import repository.*

    val customerList = await(customers.list())
    customerList.size shouldBe 2

    await(roles.list()).size shouldBe 2

    customerList foreach { customer =>
      val contractorList = await(contractors.list(customer.id))
      contractorList.size shouldBe 1
      contractorList foreach { contractor =>
        val taskList = await(tasks.list(contractor.id))
        taskList.size shouldBe 1
        taskList foreach { task =>
          val completedTask = task.copy(completed = LocalDateTime.now)
          await(tasks.save(completedTask))
        }
      }
    }

    val customersContractors = await(customers.listCustomersContractors())
    customersContractors.size shouldBe 2
    customersContractors foreach println

    val contractorsTasks = await(contractors.listContractorsTasks())
    contractorsTasks.size shouldBe 2
    contractorsTasks foreach println

    val contractorsSuppliersList = await(contractorsSuppliers.listContractorsSuppliers())
    contractorsSuppliersList.size shouldBe 2
    contractorsSuppliersList foreach println
