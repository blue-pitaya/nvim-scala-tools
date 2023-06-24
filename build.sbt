import Dependencies._

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "dev.bluepitaya"
ThisBuild / organizationName := "devtools"

lazy val root = (project in file(".")).settings(
  name := "devtools",
  // https://mvnrepository.com/artifact/org.scalameta/scalameta
  libraryDependencies += "org.scalameta" %% "scalameta" % "4.7.8",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.13" % Test
)
