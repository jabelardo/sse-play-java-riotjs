name := """sse-chat-java-riot"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.webjars" % "bootstrap" % "2.3.2",
  "org.webjars" % "jquery" % "1.12.4",
  "org.webjars" % "riot" % "3.0.2",
  "org.webjars" % "underscorejs" % "1.8.3"
)

resolvers ++= Seq(
  "Local Ivy" at s"file:///${Path.userHome}/.ivy2/local",
  "Local Maven Repository" at s"file:///${Path.userHome}/.m2/repository"
)