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
  import repository._

  def main(args: Array[String]) {
    println("Running benchmark...")
    await(createSchema())
    new PerformanceBenchMark()
    await(dropSchema())
    closeDatabase()
    println("Benchmark complete.")
  }
}

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class PerformanceBenchMark() {
  import Main.repository._

  @Benchmark
  def benchmark(): Int = await(addRole(Role(UUID.randomUUID.toString)))
}