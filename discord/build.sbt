organization := "com.hila"
name :="scala-discord"
version := "1.0.0-SNAPSHOT"


lazy val discord = project in file(".")

scalaVersion := "2.12.4"

libraryDependencies ++=Seq(
  ws
)