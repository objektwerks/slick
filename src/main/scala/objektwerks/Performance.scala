package objektwerks

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.openjdk.jmh.annotations._
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.duration._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 9)
@Fork(1)
class Performance() {
  import Peformance.repository._

  @Setup
  def setup() = exec(createSchema())

  @TearDown
  def teardown() = exec(dropSchema())

  @Benchmark
  def addRole(): Int = exec(roles.add(Role(UUID.randomUUID.toString)))

  @Benchmark
  def listRoles(): Seq[String] = exec(roles.list())
}

object Peformance extends LazyLogging {
  val config = DatabaseConfig.forConfig[H2Profile]("app", ConfigFactory.load("app.conf"))
  val repository = new Repository(config.db, 1 second)
  logger.info("Database initialized for performance testing.")
}