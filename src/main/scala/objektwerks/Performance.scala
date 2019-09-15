package objektwerks

import java.util.UUID
import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.openjdk.jmh.annotations._
import slick.basic.DatabaseConfig
import slick.jdbc.{H2Profile, JdbcProfile}

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
  def setup(): Unit = createSchema()

  @TearDown
  def teardown(): Unit = dropSchema()

  @Benchmark
  def addRole(): Int = await(roles.add(Role(name = UUID.randomUUID.toString)))

  @Benchmark
  def listRoles(): Seq[String] = await(roles.list())
}

object Peformance extends LazyLogging {
  val config = DatabaseConfig.forConfig[JdbcProfile]("app", ConfigFactory.load("app.conf"))
  val repository = new Repository(config, H2Profile, 1 second)
  logger.info("Database initialized for performance testing.")
}