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
    await(createSchema())
    schema.createStatements foreach println
  }

  override protected def afterAll(): Unit = {
    schema.dropStatements foreach println
    await(dropSchema())
    closeDatabase()
  }

  test("add > save") {
    val poolBoy = Role("pool boy")
    val yardBoy = Role("yard boy")
    await(addRole(poolBoy))
    await(addRole(yardBoy))

    val barneyId = await(saveContractor(Contractor(name = "barney", role = poolBoy.role)))
    val fredId = await(saveContractor(Contractor(name = "fred", role = yardBoy.role)))
    barneyId shouldBe 1
    fredId shouldBe 2

    val barneyTaskId = await(saveTask(Task(contractorId = barneyId, task = "clean pool", recurrence = Recurrence.weekly, completed = Some(LocalDateTime.now))))
    val fredTaskId = await(saveTask(Task(contractorId = fredId, task = "mow yard", recurrence = Recurrence.weekly, completed = Some(LocalDateTime.now))))
    barneyTaskId shouldBe 1
    fredTaskId shouldBe 2
  }

  test("find > save") {
    val barney = await(findContractor("barney"))
    val fred = await(findContractor("fred"))
    barney.id shouldBe Some(1)
    fred.id shouldBe Some(2)

    await(saveContractor(barney.copy(name = "barney rebel")))
    await(saveContractor(fred.copy(name = "fred flintstone")))
  }

  test("list") {
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
    val contractorsTasks = await(listContractorsTasks())
    contractorsTasks.size shouldBe 2
    contractorsTasks foreach println
  }
}