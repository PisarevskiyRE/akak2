ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "Akak2"
  )


lazy val akkaHttpVersion = "10.2.7"
lazy val akkaVersion     = "2.8.2"
lazy val circeVersion    = "0.14.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"                  % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed"           % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"                % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed"     % akkaVersion,
  "com.datastax.oss"  %  "java-driver-core"           % "4.15.0",
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.1.1",
  "io.circe"          %% "circe-core"                 % circeVersion,
  "io.circe"          %% "circe-generic"              % circeVersion,
  "io.circe"          %% "circe-parser"               % circeVersion,
  "de.heikoseeberger" %% "akka-http-circe"            % "1.39.2",
  "de.heikoseeberger" %% "akka-http-jackson"          % "1.39.2",
  "ch.qos.logback"    % "logback-classic"             % "1.4.6",

  // projections tutorial
//  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
//  "com.lightbend.akka" %% "akka-projection-core" % "1.4.0",
//  "com.lightbend.akka" %% "akka-projection-eventsourced" % "1.4.0",
//  "com.lightbend.akka" %% "akka-projection-cassandra" % "1.4.0"

)