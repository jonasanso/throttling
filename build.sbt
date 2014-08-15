name := "throttler"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.typesafe.akka" %% "akka-contrib" % "2.2.0"
)

play.Project.playScalaSettings
