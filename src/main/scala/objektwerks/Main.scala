package objektwerks

import com.typesafe.config.ConfigFactory
import org.openjdk.jmh.annotations.Benchmark
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.duration._

object Main {
  val config = DatabaseConfig.forConfig[H2Profile]("app", ConfigFactory.load("app.conf"))
  val repository = new Repository(config.db, 1 second)
  import repository._

  def main(args: Array[String]) {
    println("Running benchmark...")
  }

  await(createSchema())

  for (i <- 1 to 100) {
    run(Role(i.toString))
  }

  await(dropSchema())
  closeDatabase()

  @Benchmark
  def run(role: Role): Int = await(addRole(role))
}