package slick

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class SlickTest extends FunSuite with BeforeAndAfterAll {
  val respository = new Repository(ConfigFactory.load("test.conf"), "test")
  import respository._

  override protected def beforeAll(): Unit = Await.ready(db.run(createSchema), 2 seconds)

  override protected def afterAll(): Unit = {
    Await.ready(db.run(dropSchema), 2 seconds)
    db.close()
  }

  test("upsert person") {
    val fred = Person(name = "fred")
    val barney = Person(name = "barney")

    Await.ready(db.run(upsert(fred)), 1 second)
    Await.ready(db.run(upsert(barney)), 1 second)
  }

  test("find person > upsert task") {
    val futureFred = db.run(findPerson("fred"))
    val futureBarney = db.run(findPerson("barney"))

    val fred = Await.ready(futureFred, 1 second).value.get.get
    val barney = Await.ready(futureBarney, 1 second).value.get.get

    val futureFredTask = db.run(upsert(Task(personId = fred.id.get, task = "Mow yard.")))
    val futureBarneyTask = db.run(upsert(Task(personId = barney.id.get, task = "Clean pool.")))

    Await.ready(futureFredTask, 1 second)
    Await.ready(futureBarneyTask, 1 second)
  }

  test("list persons and tasks") {
    val futurePersons = db.run(listPersons)
    val persons = Await.ready(futurePersons, 1 second).value.get.get
    assert(persons.size == 2)
    for (p <- persons) {
      println(p)
      val futureTasks = db.run(listTasks(p))
      val tasks = Await.ready(futureTasks, 1 second).value.get.get
      assert(tasks.size == 1)
      for (t <- tasks) {
        println(s"\t$t")
      }
    }
  }
}