package slick

import java.time.LocalDateTime

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class StoreTest extends FunSuite with BeforeAndAfterAll with Matchers {
  val store = new Store(path = "test", config = ConfigFactory.load("test.conf")) with Repository
  import store._

  override protected def beforeAll(): Unit = Await.result(db.run(createSchema), 1 second)

  override protected def afterAll(): Unit = {
    Await.result(db.run(dropSchema), 1 second)
    db.close()
  }

  test("person > task") {
    val barneyId = Await.result(db.run(addPerson("barney")), 1 second)
    val fredId = Await.result(db.run(addPerson("fred")), 1 second)
    barneyId shouldBe 1
    fredId shouldBe 2

    val barney = Await.result(db.run(findPerson("barney")), 1 second)
    val fred = Await.result(db.run(findPerson("fred")), 1 second)
    barney.id shouldBe Some(1)
    fred.id shouldBe Some(2)

    val barneyTaskId = Await.result(db.run(addTask(barneyId, "clean pool")), 1 second)
    val fredTaskId = Await.result(db.run(addTask(fredId, "mow yard")), 1 second)
    barneyTaskId shouldBe 1
    fredTaskId shouldBe 2

    val persons = Await.result(db.run(listPersons), 1 second)
    persons.size shouldBe 2
    persons foreach println
    persons foreach { p =>
      val tasks = Await.result(db.run(listTasks(p)), 1 second)
      tasks.size shouldBe 1
      tasks foreach { t =>
        val completedTask = t.copy(completed = Some(LocalDateTime.now))
        Await.result(db.run(updateTask(completedTask)), 1 second)
        println(completedTask)
      }
    }
    Await.result(db.run(listPersonsTasks), 1 second).foreach(println)
  }
}