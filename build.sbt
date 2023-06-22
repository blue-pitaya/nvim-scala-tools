import Dependencies._

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file(".")).settings(
  name := "tools",
  // https://mvnrepository.com/artifact/org.scalameta/scalameta
  libraryDependencies += "org.scalameta" %% "scalameta" % "4.7.8",
  libraryDependencies += munit % Test
)
