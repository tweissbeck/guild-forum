name := """forum"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  // jdbc is not compatible with play slick
  jdbc,
  "com.typesafe.play" %% "anorm" % "2.5.0",
  cache,
  ws,
//  "com.typesafe.play" %% "play-slick" % "2.0.0",
//  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "com.h2database" % "h2" % "1.4.192",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  evolutions
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
