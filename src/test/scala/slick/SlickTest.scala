package slick

import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SlickTest extends FunSuite with BeforeAndAfterAll {
  override protected def beforeAll(): Unit = {
    super.beforeAll
    Store.createSchema()
  }

  override protected def afterAll(): Unit = {
    super.afterAll
    Store.dropSchema()
    Store.close()
  }

  test("insert person") {
    val fred = Person(name = "fred")
    val barney = Person(name = "barney")
    val futureFred = Store.insert(fred)
    val futureBarney = Store.insert(barney)
    val fredId = Await.ready(futureFred, Duration.Inf).value.get.get
    val barneyId = Await.ready(futureBarney, Duration.Inf).value.get.get
    println(s"Fred inserted autoinc id: $fredId")
    println(s"Barney inserted autoinc id: $barneyId")
    assert(fredId > 0)
    assert(barneyId > 0)
  }

  test("insert task") {
    val futureFred = Store.findPerson("fred")
    val futureBarney = Store.findPerson("barney")
    val fred = Await.ready(futureFred, Duration.Inf).value.get.get
    val barney = Await.ready(futureBarney, Duration.Inf).value.get.get
    val futureFredTask = Store.insert(Task(personId = fred.id.get, task = "Mow yard."))
    val futureBarneyTask = Store.insert(Task(personId = barney.id.get, task = "Clean pool."))
    val fredTaskId = Await.ready(futureFredTask, Duration.Inf).value.get.get
    val barneyTaskId = Await.ready(futureBarneyTask, Duration.Inf).value.get.get
    println(s"Fred inserted task autoinc id: $fredTaskId")
    println(s"Barney inserted task autoinc id: $barneyTaskId")
    assert(fredTaskId > 0)
    assert(barneyTaskId > 0)
  }

  test("list persons and tasks") {
    val futurePersons = Store.listPersons
    val persons = Await.ready(futurePersons, Duration.Inf).value.get.get
    assert(persons.size == 2)
    for (p <- persons) {
      println(p)
      val futureTasks = Store.listTasks(p)
      val tasks = Await.ready(futureTasks, Duration.Inf).value.get.get
      assert(tasks.size == 1)
      for (t <- tasks) {
        println(s"\t$t")
      }
    }
  }
}