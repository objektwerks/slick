package objektwerks

import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.H2Profile

import scala.concurrent.duration._

object Main extends App {
  val config = DatabaseConfig.forConfig[H2Profile]("app", ConfigFactory.load("app.conf"))
  val repository = new Repository(config.db, 1 second)
  import repository._

  await(createSchema())

  val poolBoy = Role("pool boy")
  val yardBoy = Role("yard boy")
  await(addRole(poolBoy))
  await(addRole(yardBoy))
  val roles = await(listRoles())
  assert(roles.size == 2)

  await(dropSchema())
  closeDatabase()
}