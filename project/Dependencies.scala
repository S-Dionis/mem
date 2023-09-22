import sbt._

object Dependencies {

  lazy val ZioVersion       = "2.0.16"
  lazy val LiquibaseVersion = "3.4.2"
  lazy val PostgresVersion  = "42.3.1"
  lazy val ZIOHttpVersion   = "3.0.0-RC2"
  lazy val sprayJsonVersion = "1.3.6"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.11"

  lazy val zio: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio"                 % ZioVersion,
    "dev.zio" %% "zio-test"            % ZioVersion,
    "dev.zio" %% "zio-test-sbt"        % ZioVersion,
    "dev.zio" %% "zio-http"            % "3.0.0-RC2",
    "dev.zio" %% "zio-config"          % "3.0.7",
    "dev.zio" %% "zio-config-magnolia" % "3.0.7",
    "dev.zio" %% "zio-config-typesafe" % "3.0.7"
  )

  lazy val quill = Seq(
    "io.getquill"          %% "quill-jdbc-zio" % "4.6.1",
    "io.github.kitlangton" %% "zio-magic"      % "0.3.11"
  )

  lazy val liquibase = Seq("org.liquibase" % "liquibase-core" % LiquibaseVersion)

  lazy val postgres = Seq("org.postgresql" % "postgresql" % PostgresVersion)

  lazy val sprayJson = Seq("io.spray" %% "spray-json" % sprayJsonVersion)

}
