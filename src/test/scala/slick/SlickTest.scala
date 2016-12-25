package slick

import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SlickTest extends FunSuite with BeforeAndAfterAll {
  val store = new Store(ConfigFactory.load("test.conf"), "test")
  import store._

  override protected def beforeAll(): Unit = {
    store.createSchema()
  }

  override protected def afterAll(): Unit = {
    store.dropSchema()
    store.close()
  }

  test("insert person") {
    val fred = Person(name = "fred")
    val barney = Person(name = "barney")
    val futureFred = db.run(upsert(fred))
    val futureBarney = db.run(upsert(barney))
    val fredId = Await.ready(futureFred, Duration.Inf).value.get.get
    val barneyId = Await.ready(futureBarney, Duration.Inf).value.get.get
    println(s"Fred inserted autoinc id: $fredId")
    println(s"Barney inserted autoinc id: $barneyId")
    assert(fredId > 0)
    assert(barneyId > 0)
  }

  test("insert task") {
    val futureFred = db.run(findPerson("fred"))
    val futureBarney = db.run(findPerson("barney"))
    val fred = Await.ready(futureFred, Duration.Inf).value.get.get
    val barney = Await.ready(futureBarney, Duration.Inf).value.get.get
    val futureFredTask = db.run(upsert(Task(personId = fred.id.get, task = "Mow yard.")))
    val futureBarneyTask = db.run(upsert(Task(personId = barney.id.get, task = "Clean pool.")))
    val fredTaskId = Await.ready(futureFredTask, Duration.Inf).value.get.get
    val barneyTaskId = Await.ready(futureBarneyTask, Duration.Inf).value.get.get
    println(s"Fred inserted task autoinc id: $fredTaskId")
    println(s"Barney inserted task autoinc id: $barneyTaskId")
    assert(fredTaskId > 0)
    assert(barneyTaskId > 0)
  }

  test("list persons and tasks") {
    val futurePersons = db.run(listPersons)
    val persons = Await.ready(futurePersons, Duration.Inf).value.get.get
    assert(persons.size == 2)
    for (p <- persons) {
      println(p)
      val futureTasks = db.run(listTasks(p))
      val tasks = Await.ready(futureTasks, Duration.Inf).value.get.get
      assert(tasks.size == 1)
      for (t <- tasks) {
        println(s"\t$t")
      }
    }
  }
}