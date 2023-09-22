ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "mem"
  )

libraryDependencies ++= Dependencies.zio
libraryDependencies ++= Dependencies.postgres
libraryDependencies ++= Dependencies.liquibase
libraryDependencies ++= Dependencies.quill