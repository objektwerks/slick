package slick

import java.time.LocalDateTime

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class RepositoryTest extends FunSuite with BeforeAndAfterAll with Matchers {
  val repository = new Repository(path = "test", config = ConfigFactory.load("test.conf"))
  import repository._

  override protected def beforeAll(): Unit = Await.result(createSchema(), 1 second)

  override protected def afterAll(): Unit = {
    Await.result(dropSchema(), 1 second)
    close()
  }

  test("person > task") {
    val barneyId = Await.result(addPerson("barney"), 1 second)
    val fredId = Await.result(addPerson("fred"), 1 second)
    barneyId shouldBe 1
    fredId shouldBe 2

    val barney = Await.result(findPerson("barney"), 1 second)
    val fred = Await.result(findPerson("fred"), 1 second)
    barney.id shouldBe Some(1)
    fred.id shouldBe Some(2)

    val barneyTaskId = Await.result(addTask(barneyId, "clean pool"), 1 second)
    val fredTaskId = Await.result(addTask(fredId, "mow yard"), 1 second)
    barneyTaskId shouldBe 1
    fredTaskId shouldBe 2

    val persons = Await.result(listPersons(), 1 second)
    persons.size shouldBe 2
    persons foreach { p =>
      val tasks = Await.result(listTasks(p), 1 second)
      tasks.size shouldBe 1
      tasks foreach { t =>
        val completedTask = t.copy(completed = Some(LocalDateTime.now))
        Await.result(updateTask(completedTask), 1 second)
      }
    }
    Await.result(listPersonsTasks(), 1 second).foreach(println)
  }
}