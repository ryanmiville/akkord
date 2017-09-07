name := "akkord-experimental"

version := "0.1"

organization := "com.github.ryanmiville"

licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

scalaVersion := "2.12.3"

scalacOptions ++= Seq(
  "-feature"
)
val akkaVersion = "2.4.19"

val akkaHttpVersion = "10.0.9"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
)

val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics"
).map(_ % circeVersion)

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies += "de.heikoseeberger" %% "akka-http-circe" % "1.17.0"