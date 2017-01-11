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

    val barneyId = await(saveWorker(Worker(name = "barney", role = poolBoy.role)))
    val fredId = await(saveWorker(Worker(name = "fred", role = yardBoy.role)))
    barneyId shouldBe 1
    fredId shouldBe 2

    val barneyTaskId = await(saveTask(Task(workerId = barneyId, task = "clean pool", recurrence = Recurrence.weekly)))
    val fredTaskId = await(saveTask(Task(workerId = fredId, task = "mow yard", recurrence = Recurrence.weekly)))
    barneyTaskId shouldBe 1
    fredTaskId shouldBe 2
  }

  test("find > save") {
    val barney = await(findWorker("barney"))
    val fred = await(findWorker("fred"))
    barney.id shouldBe Some(1)
    fred.id shouldBe Some(2)

    await(saveWorker(barney.copy(name = "barney rebel")))
    await(saveWorker(fred.copy(name = "fred flintstone")))
  }

  test("list") {
    val roles = await(listRoles())
    roles.size shouldBe 2

    val workers = await(listWorkers())
    workers.size shouldBe 2
    workers foreach { p =>
      val tasks = await(listTasks(p))
      tasks.size shouldBe 1
      tasks foreach { t =>
        val completedTask = t.copy(completed = Some(LocalDateTime.now))
        await(saveTask(completedTask))
      }
    }
    val workersTasks = await(listWorkersTasks())
    workersTasks.size shouldBe 2
    workersTasks foreach println
  }
}