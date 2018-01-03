organization := "com.hila"
name := """forum"""
version := "1.0.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .aggregate(discord).dependsOn(discord)

lazy val discord = project

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  guice,
  // DATA BASE
  jdbc, //jdbc is not compatible with play slick
  "com.typesafe.play" %% "anorm" % "2.5.3",
  //  "com.h2database" % "h2" % "1.4.192",
  "org.postgresql" % "postgresql" % "42.1.2",
  evolutions,

  // PLAY
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test,
  // JWT
  "org.bitbucket.b_c" % "jose4j" % "0.5.1",
  "com.typesafe.play" %% "play-json" % "2.6.0"
)

// The Typesafe repository
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/maven-releases/"
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
