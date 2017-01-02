package slick

import com.typesafe.config.Config
import slick.jdbc.H2Profile.api._

class Store(path: String, config: Config) {
  val db = Database.forConfig(path, config)
}