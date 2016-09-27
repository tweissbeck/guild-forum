organization := "com.hila"
name := """forum"""
version := "1.0.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .aggregate(discord).dependsOn(discord)

lazy val discord = project

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  // DATA BASE
  jdbc, //jdbc is not compatible with play slick
  "com.typesafe.play" %% "anorm" % "2.5.0",
  "com.h2database" % "h2" % "1.4.192",
  evolutions,

  // PLAY
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,

  // JWT
  "org.bitbucket.b_c" % "jose4j" % "0.5.1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
