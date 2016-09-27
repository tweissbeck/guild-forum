organization := "com.hila"
name :="scala-discord"
version := "1.0.0-SNAPSHOT"


lazy val discord = project in file(".")

scalaVersion := "2.11.7"

libraryDependencies ++=Seq(
  ws
)