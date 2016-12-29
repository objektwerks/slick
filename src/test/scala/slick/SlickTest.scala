package slick

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration._

class SlickTest extends FunSuite with BeforeAndAfterAll {
  val respository = new Repository(ConfigFactory.load("test.conf"), "test")
  import respository._

  override protected def beforeAll(): Unit = {
    val future = db.run(createSchema)
    Await.ready(future, 3 seconds)
  }

  override protected def afterAll(): Unit = {
    val future = db.run(dropSchema)
    Await.ready(future, 3 seconds)
    db.close()
  }

  test("upsert person") {
    val fred = Person(name = "fred")
    val barney = Person(name = "barney")

    val futureFred = db.run(upsert(fred))
    val futureBarney = db.run(upsert(barney))

    val fredId = Await.ready(futureFred, 3 seconds).value.get.get
    val barneyId = Await.ready(futureBarney, 3 seconds).value.get.get

    println(s"Fred inserted autoinc id: $fredId")
    println(s"Barney inserted autoinc id: $barneyId")

    assert(fredId > 0)
    assert(barneyId > 0)
  }

  test("upsert task") {
    val futureFred = db.run(findPerson("fred"))
    val futureBarney = db.run(findPerson("barney"))

    val fred = Await.ready(futureFred, 3 seconds).value.get.get
    val barney = Await.ready(futureBarney, 3 seconds).value.get.get

    val futureFredTask = db.run(upsert(Task(personId = fred.id.get, task = "Mow yard.")))
    val futureBarneyTask = db.run(upsert(Task(personId = barney.id.get, task = "Clean pool.")))

    val fredTaskId = Await.ready(futureFredTask, 3 seconds).value.get.get
    val barneyTaskId = Await.ready(futureBarneyTask, 3 seconds).value.get.get

    println(s"Fred inserted task autoinc id: $fredTaskId")
    println(s"Barney inserted task autoinc id: $barneyTaskId")

    assert(fredTaskId > 0)
    assert(barneyTaskId > 0)
  }

  test("list persons and tasks") {
    val futurePersons = db.run(listPersons)
    val persons = Await.ready(futurePersons, 3 seconds).value.get.get
    assert(persons.size == 2)
    for (p <- persons) {
      println(p)
      val futureTasks = db.run(listTasks(p))
      val tasks = Await.ready(futureTasks, 3 seconds).value.get.get
      assert(tasks.size == 1)
      for (t <- tasks) {
        println(s"\t$t")
      }
    }
  }
}