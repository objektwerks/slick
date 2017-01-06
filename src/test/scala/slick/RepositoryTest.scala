package slick

import java.time.LocalDateTime

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.duration._

class RepositoryTest extends FunSuite with BeforeAndAfterAll with Matchers {
  val config = DatabaseConfig.forConfig[H2Profile]("test", ConfigFactory.load("test.conf"))
  val repository = new Repository(config.db)
  import repository._

  override protected def beforeAll(): Unit = {
    await(createSchema(), 1 second)
    schema.createStatements foreach println
  }

  override protected def afterAll(): Unit = {
    schema.dropStatements foreach println
    await(dropSchema(), 1 second)
    closeDatabase()
  }

  test("add") {
    val poolBoy = Role("pool boy")
    val yardBoy = Role("yard boy")
    await(addRole(poolBoy), 1 second)
    await(addRole(yardBoy), 1 second)

    val barneyId = await(saveWorker(Worker(name = "barney", role = poolBoy.role, recurrence = Recurrence.weekly)), 1 second)
    val fredId = await(saveWorker(Worker(name = "fred", role = yardBoy.role, recurrence = Recurrence.biweekly)), 1 second)
    barneyId shouldBe 1
    fredId shouldBe 2

    val barneyTaskId = await(saveTask(Task(workerId = barneyId, task = "clean pool")), 1 second)
    val fredTaskId = await(saveTask(Task(workerId = fredId, task = "mow yard")), 1 second)
    barneyTaskId shouldBe 1
    fredTaskId shouldBe 2
  }

  test("find") {
    val barney = await(findWorker("barney"), 1 second)
    val fred = await(findWorker("fred"), 1 second)
    barney.id shouldBe Some(1)
    fred.id shouldBe Some(2)
  }

  test("list") {
    val roles = await(listRoles(), 1 second)
    roles.size shouldBe 2

    val workers = await(listWorkers(), 1 second)
    workers.size shouldBe 2
    workers foreach { p =>
      val tasks = await(listTasks(p), 1 second)
      tasks.size shouldBe 1
      tasks foreach { t =>
        val completedTask = t.copy(completed = Some(LocalDateTime.now))
        await(saveTask(completedTask), 1 second)
      }
    }
    val workersTasks = await(listWorkersTasks(), 1 second)
    workersTasks.size shouldBe 2
  }
}