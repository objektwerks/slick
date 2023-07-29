enablePlugins(JmhPlugin)

name := "slick"
organization := "objketwerks"
version := "0.1-SNAPSHOT"
scalaVersion := "3.3.1-RC3"
libraryDependencies ++= {
  val slickVersion = "3.5.0-M4"
  Seq(
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
    "com.h2database" % "h2" % "2.1.220",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "ch.qos.logback" % "logback-classic" % "1.4.8",
    "org.scalatest" %% "scalatest" % "3.2.16" % Test
  )
}
scalacOptions ++= Seq(
  "-Wunused:all"
)
