package objektwerks

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.duration._

object Store extends LazyLogging {
  val config = DatabaseConfig.forConfig[H2Profile]("app", ConfigFactory.load("app.conf"))
  val repository = new Repository(config.db, 1 second)
  import repository._

  await(createSchema())
  sys.addShutdownHook {
    await(dropSchema())
    closeDatabase()
  }

  logger.info("Store initialized.")
}