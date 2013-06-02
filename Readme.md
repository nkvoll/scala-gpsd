# Scala (and Akka) library to interface with GPSD

This repository contains some sample code to interface with [gpsd](http://gpsd.berlios.de/),
including an [Akka](http://akka.io/) actor which acts as a client to the `gpsd` service.

The three projects in this repository are:

 - The `gpsd` client [library code](src/main/scala/org/nkvoll/gpsd/client/messages)
 - An Akka actor [GPSClient](akka/src/main/scala/org/nkvoll/gpsd/client/akka/GPSClient.scala)
 - An [example application](example/src/main/scala/org/nkvoll/gpsd/client/example/Bootstrap.scala) that uses the `GPSClient` actor.

Note: Not all `gpsd`-types are parsed by the actor, but the most commonly used are:

 - TPV
 - SKY
 - WATCH
 - VERSION
 - DEVICES
 - DEVICE

Open an issue (or even better: a pull request) if you want to add more.

## To run the example:

Locate one or more dumps from `http://download.geonames.org/export/dump/` for your location:

    $ wget http://download.geonames.org/export/dump/NO.zip

Build a simple location database using `CreateDatabase`:

    $ sbt "example/run-main org.nkvoll.gpsd.example.CreateDatabase NO.zip"

See [reference.conf](example/src/main/resources/reference.conf) for configuration options. Run the example (if you want to provide your own `reference.conf`, add `-Dconfig.file=yourfile.conf` just after `sbt`:

    $ sbt "example/run-main org.nkvoll.gpsd.example.Bootstrap"

Given that the connected gps works, the database as been created and we're able to connect to the `gpsd` service, it should log the name and gps coordinates of a few of the closest "cities".

## License

MIT-licensed. See [LICENSE.txt](LICENSE.txt) for details.

## Links

Akka: [http://akka.io/](http://akka.io/)
GPSD: [http://gpsd.berlios.de/](http://gpsd.berlios.de/)