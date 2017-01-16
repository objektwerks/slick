package objektwerks

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import org.openjdk.jmh.annotations._
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.duration._

object Main {
  val config = DatabaseConfig.forConfig[H2Profile]("app", ConfigFactory.load("app.conf"))
  val repository = new Repository(config.db, 1 second)

  def main(args: Array[String]) {
    println("Running benchmark...")
    new PerformanceBenchMark(repository)
    println("Benchmark complete.")
  }
}

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class PerformanceBenchMark(@State(Scope.Thread) repository: Repository) {
  import repository._

  @Setup
  def setup(): Unit = await(createSchema())

  @TearDown
  def teardown(): Unit = {
    await(dropSchema())
    closeDatabase()
  }

  @Benchmark
  def benchmark(): Int = await(addRole(Role(UUID.randomUUID.toString)))
}