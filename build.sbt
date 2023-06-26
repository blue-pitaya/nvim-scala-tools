import Dependencies._

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "dev.bluepitaya"
ThisBuild / organizationName := "devtools"

lazy val root = (project in file(".")).settings(
  name := "devtools",
  libraryDependencies += "org.scalameta" %% "scalameta" % "4.7.8",
  libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.0",
  libraryDependencies += "co.fs2" %% "fs2-core" % "3.7.0",
  libraryDependencies += "co.fs2" %% "fs2-io" % "3.7.0",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.13" % Test
)
