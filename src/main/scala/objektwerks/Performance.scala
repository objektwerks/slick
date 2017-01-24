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
  def setup() = createSchema()

  @TearDown
  def teardown() = dropSchema()

  @Benchmark
  def role(): Int = await(addRole(Role(UUID.randomUUID.toString)))

  @Benchmark
  def roles(): Seq[String] = await(listRoles())
}

object Peformance extends LazyLogging {
  val config = DatabaseConfig.forConfig[H2Profile]("app", ConfigFactory.load("app.conf"))
  val repository = new Repository(config.db, 1 second)
  logger.info("Database initialized for performance testing.")
}