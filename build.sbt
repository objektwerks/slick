enablePlugins(JmhPlugin)

name := "slick"
organization := "objketwerks"
version := "0.4-SNAPSHOT"
scalaVersion := "3.6.4-RC2"
libraryDependencies ++= {
  val slickVersion = "3.5.2"
  Seq(
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "com.h2database" % "h2" % "2.3.232",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "ch.qos.logback" % "logback-classic" % "1.5.17",
    "org.scalatest" %% "scalatest" % "3.2.19" % Test
  )
}
scalacOptions ++= Seq(
  "-Wunused:all"
)
