import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object Build extends Build {
  val akkaVersion = "2.2-M3"
  val logbackVersion = "1.0.13"
  val scalikejdbcVersion = "[1.6,)"

  val defaultBuildSettings = Seq(
    version := "0.1.0-SNAPSHOT",
    organization := "org.nkvoll",
    scalaVersion := "2.10.1"
  ) ++ Project.defaultSettings

  lazy val root = Project("gpsd", file(".")).settings(defaultBuildSettings: _*).settings(
    name := "gpsd",

    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2",

    libraryDependencies += "joda-time" % "joda-time" % "2.2",
    libraryDependencies += "org.joda" % "joda-convert" % "1.3.1",

    libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5",

    libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"
  )

  lazy val akka = Project("akka", file("akka")).dependsOn(root).settings(defaultBuildSettings: _*).settings(
    name := "gpsd-akka",

    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,

    libraryDependencies += "ch.qos.logback" % "logback-core" % logbackVersion,
    libraryDependencies += "ch.qos.logback" % "logback-classic" % logbackVersion
  )

  lazy val example = Project("example", file("example")).dependsOn(akka).settings(defaultBuildSettings: _*).settings(assemblySettings: _*).settings(
    name := "gpsd-example",

    mainClass in assembly := Some("org.nkvoll.gpsd.example.Bootstrap")
  ).settings(
    libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.7.2",
    libraryDependencies += "com.github.seratch" %% "scalikejdbc" % scalikejdbcVersion,
    libraryDependencies += "com.github.seratch" %% "scalikejdbc-interpolation" % scalikejdbcVersion
  )
}