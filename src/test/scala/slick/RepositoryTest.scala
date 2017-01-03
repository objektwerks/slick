package slick

import java.time.LocalDateTime

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.duration._

class RepositoryTest extends FunSuite with BeforeAndAfterAll with Matchers {
  val repository = new Repository(path = "test", config = ConfigFactory.load("test.conf"))
  import repository._

  override protected def beforeAll(): Unit = await(createSchema(), 1 second)

  override protected def afterAll(): Unit = {
    await(dropSchema(), 1 second)
    close()
  }

  test("add") {
    val barneyId = await(addPerson("barney"), 1 second)
    val fredId = await(addPerson("fred"), 1 second)
    barneyId shouldBe 1
    fredId shouldBe 2

    val barneyTaskId = await(addTask(barneyId, "clean pool"), 1 second)
    val fredTaskId = await(addTask(fredId, "mow yard"), 1 second)
    barneyTaskId shouldBe 1
    fredTaskId shouldBe 2
  }

  test("find") {
    val barney = await(findPerson("barney"), 1 second)
    val fred = await(findPerson("fred"), 1 second)
    barney.id shouldBe Some(1)
    fred.id shouldBe Some(2)
  }

  test("list") {
    val persons = await(listPersons(), 1 second)
    persons.size shouldBe 2
    persons foreach { p =>
      val tasks = await(listTasks(p), 1 second)
      tasks.size shouldBe 1
      tasks foreach { t =>
        val completedTask = t.copy(completed = Some(LocalDateTime.now))
        await(updateTask(completedTask), 1 second)
      }
    }
    val personsTasks = await(listPersonsTasks(), 1 second)
    personsTasks.size shouldBe 2
  }
}