name := "slick"
organization := "objketwerks"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.1"
ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
libraryDependencies ++= {
  val slickVersion = "3.2.0-M2"
  Seq(
    "com.typesafe.slick" % "slick_2.12" % slickVersion % "test",
    "com.typesafe.slick" % "slick-hikaricp_2.12" % slickVersion % "test",
    "com.h2database" % "h2" % "1.4.193" % "test",
    "ch.qos.logback" % "logback-classic" % "1.1.7" % "test",
    "org.openjdk.jmh" % "jmh-core" % "1.17.3" % "test",
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
javaOptions += "-server -Xss1m -Xmx2g"

enablePlugins(JmhPlugin)