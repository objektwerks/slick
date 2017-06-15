name := "slick"
organization := "objketwerks"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.2"
ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
libraryDependencies ++= {
  val slickVersion = "3.2.0"
  Seq(
    "com.typesafe.slick" % "slick_2.12" % slickVersion,
    "com.typesafe.slick" % "slick-hikaricp_2.12" % slickVersion,
    "com.h2database" % "h2" % "1.4.193",
    "com.typesafe.scala-logging" % "scala-logging_2.12" % "3.5.0",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "org.scalatest" % "scalatest_2.12" % "3.0.1" % "test"
  )
}
scalacOptions ++= Seq(
  "-language:postfixOps",
  "-language:reflectiveCalls",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-feature",
  "-Ywarn-unused-import",
  "-Ywarn-unused",
  "-Ywarn-dead-code",
  "-unchecked",
  "-deprecation",
  "-Xfatal-warnings",
  "-Xlint:missing-interpolator",
  "-Xlint"
)
fork in test := true
enablePlugins(JmhPlugin)
clippyColorsEnabled := true
