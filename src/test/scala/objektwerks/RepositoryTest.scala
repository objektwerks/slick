package objektwerks

import com.typesafe.config.ConfigFactory

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import slick.basic.DatabaseConfig
import slick.jdbc.{H2Profile, JdbcProfile}

import scala.concurrent.duration._
import scala.language.postfixOps

final class RepositoryTest extends AnyFunSuite with BeforeAndAfterAll with Matchers:
  val config = DatabaseConfig.forConfig[JdbcProfile]("test", ConfigFactory.load("test.conf"))
  val repository = Repository(config, H2Profile, 1 second)

  override protected def beforeAll(): Unit =
    repository.createSchema()

  override protected def afterAll(): Unit =
    repository.dropSchema()
    repository.close()