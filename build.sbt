ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "akka-kafka-spark"
  )

enablePlugins(AkkaGrpcPlugin)
val kafkaVersion = "3.0.1"
val AkkaVersion = "2.6.19"
val AlpakkaKafkaVersion = "3.0.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-kafka" % AlpakkaKafkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion ,
  "org.apache.kafka" % "kafka-clients" % kafkaVersion
)
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.11"