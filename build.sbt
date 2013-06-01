name := "HelloGPS"

scalaVersion := "2.10.1"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2-M3"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.5"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.0.13"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies += "io.spray" %%  "spray-json" % "1.2.5"