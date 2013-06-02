import AssemblyKeys._

name := "HelloGPS"

version := "0.1.0-SNAPSHOT"

organization := "org.nkvoll"

scalaVersion := "2.10.1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2-M3"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.2-M3"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.0.13"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2"

libraryDependencies += "joda-time" % "joda-time" % "2.2"

libraryDependencies += "org.joda" % "joda-convert" % "1.3.1"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/snapshots"

libraryDependencies ++= Seq(
  "com.github.seratch" %% "scalikejdbc" % "[1.6,)",
  "com.github.seratch" %% "scalikejdbc-interpolation" % "[1.6,)",
  //"postgresql" % "postgresql" % "9.1-901.jdbc4",  // your JDBC driver
  "org.xerial" % "sqlite-jdbc" % "3.7.2"
)

assemblySettings

mainClass in assembly := Some("org.nkvoll.gpsd.Bootstrap")