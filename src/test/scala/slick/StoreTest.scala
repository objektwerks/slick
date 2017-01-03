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
    Await.result(db.run(upsert(Person(name = "barney")) andThen upsert(Person(name = "fred"))), 1 second)

    val barney = Await.result(db.run(findPerson("barney")), 1 second)
    val fred = Await.result(db.run(findPerson("fred")), 1 second)

    Await.result(db.run(upsert(Task(personId = barney.id.get, task = "clean pool"))), 1 second)
    Await.result(db.run(upsert(Task(personId = fred.id.get, task = "mow yard"))), 1 second)

    val persons = Await.result(db.run(listPersons), 1 second)
    persons.size shouldBe 2
    persons foreach println
    persons foreach { p =>
      val tasks = Await.result(db.run(listTasks(p)), 1 second)
      tasks.size shouldBe 1
      tasks foreach { t =>
        val completedTask = t.copy(completed = Some(LocalDateTime.now))
        Await.result(db.run(upsert(completedTask)), 1 second)
        println(completedTask)
      }
    }
    Await.result(db.run(listPersonTask), 1 second).foreach(println)
  }
}