package objektwerks

import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Mode, OutputTimeUnit}
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.duration._

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
object Main extends App {
  val config = DatabaseConfig.forConfig[H2Profile]("app", ConfigFactory.load("app.conf"))
  val repository = new Repository(config.db, 1 second)
  import repository._

  await(createSchema())

  for (i <- 1 to 100) {
    run(Role(i.toString))
  }

  await(dropSchema())
  closeDatabase()

  @Benchmark
  def run(role: Role): Int = await(addRole(role))
}