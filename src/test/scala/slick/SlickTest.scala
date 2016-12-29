package slick

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class SlickTest extends FunSuite with BeforeAndAfterAll {
  val respository = new Repository(ConfigFactory.load("test.conf"), "test")
  import respository._

  override protected def beforeAll(): Unit = Await.result(db.run(createSchema), 1 second)

  override protected def afterAll(): Unit = {
    Await.result(db.run(dropSchema), 1 second)
    db.close()
  }

  test("upsert person") {
    Await.result(db.run(upsert(Person(name = "fred"))), 1 second)
    Await.result(db.run(upsert(Person(name = "barney"))), 1 second)
  }

  test("find person > upsert task") {
    val fred = Await.result(db.run(findPerson("fred")), 1 second)
    val barney = Await.result(db.run(findPerson("barney")), 1 second)

    Await.result(db.run(upsert(Task(personId = fred.id.get, task = "Mow yard."))), 1 second)
    Await.result(db.run(upsert(Task(personId = barney.id.get, task = "Clean pool."))), 1 second)
  }

  test("list persons and tasks") {
    val futurePersons = db.run(listPersons)
    val persons = Await.result(futurePersons, 1 second)
    assert(persons.size == 2)
    for (p <- persons) {
      println(p)
      val futureTasks = db.run(listTasks(p))
      val tasks = Await.result(futureTasks, 1 second)
      assert(tasks.size == 1)
      for (t <- tasks) {
        println(s"\t$t")
      }
    }
  }
}