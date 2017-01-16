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

  await(createSchema())

  def main(args: Array[String]) {
    println("Start ***********************************************************")
    sys.addShutdownHook {
      await(dropSchema())
      closeDatabase()
    }
    println("End ************************************************************")
  }
}

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
class PerformanceBenchmark() {
  import Main.repository._

  @Benchmark
  def role(): Int = await(addRole(Role(UUID.randomUUID.toString)))
}