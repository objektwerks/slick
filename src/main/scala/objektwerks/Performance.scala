package objektwerks

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._

import scala.concurrent.duration._
import scala.language.postfixOps

import slick.basic.DatabaseConfig
import slick.jdbc.{H2Profile, JdbcProfile}

object Peformance extends LazyLogging {
  val config = DatabaseConfig.forConfig[JdbcProfile]("app", ConfigFactory.load("app.conf"))
  val repository = Repository(config, H2Profile, 1 second)
  logger.info("Database and Repository initialized for performance testing.")
}

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@Fork(1)
class Performance() {
  import Peformance.repository._

  @Setup
  def setup(): Unit = createSchema()

  @TearDown
  def teardown(): Unit = {
    dropSchema()
    close()
  }

  @Benchmark
  def addRole(): Int = await( roles.add( Role(name = UUID.randomUUID.toString) ) )

  @Benchmark
  def listRoles(): Seq[String] = await( roles.list() )
}