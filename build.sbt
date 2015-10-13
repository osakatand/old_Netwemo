name := """netwemo"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "1.6.0"
