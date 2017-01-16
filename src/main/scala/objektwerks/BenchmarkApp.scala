package objektwerks

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import org.openjdk.jmh.annotations._
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.duration._

object BenchmarkApp {
  val config = DatabaseConfig.forConfig[H2Profile]("app", ConfigFactory.load("app.conf"))
  val repository = new Repository(config.db, 1 second)
  import repository._

  await(createSchema())

  def main(args: Array[String]) {
    sys.addShutdownHook {
      await(dropSchema())
      closeDatabase()
    }
  }
}

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@Fork(1)
class PeformanceBenchmark() {
  import BenchmarkApp.repository._

  @Benchmark
  def role(): Int = await(addRole(Role(UUID.randomUUID.toString)))
}