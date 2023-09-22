package ru.otus

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.getquill.{NamingStrategy, PostgresZioJdbcContext, SnakeCase}
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource._
import ru.otus.configuration.Config
import zio._
import zio.config.getConfig

import java.sql.DriverManager

package object database {

  object Ctx extends PostgresZioJdbcContext(NamingStrategy(SnakeCase))

  object LiquibaseService {

    val migration: ZLayer[configuration.Config, Throwable, Unit] = ZLayer {
      ZIO.scoped {
        for {
          conf <- getConfig[configuration.Config]
          conn = ZIO.attempt(new JdbcConnection(DriverManager.getConnection(conf.dbConfig.url, conf.dbConfig.user, conf.dbConfig.password)))
          dbConn <- ZIO.acquireRelease(conn)(c => ZIO.succeed(c.close()))
          _ <- ZIO.attempt(new Liquibase(conf.dbConfig.migrationFile, new ClassLoaderResourceAccessor(getClass.getClassLoader), DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbConn)).update("dev"))
        } yield ()
      }
    }
  }

  object Datasource {

    def datasource = ZLayer.fromZIO {
      for {
        _ <- Console.printLine("datasource init")
        config <- ZIO.service[Config]
        hikariConf <- ZIO.attempt {
          val hc = new HikariConfig()
          hc.setSchema(config.dbConfig.schema)
          hc.setPassword(config.dbConfig.password)
          hc.setJdbcUrl(config.dbConfig.url)
          hc.setDriverClassName(config.dbConfig.driver)
          hc.setUsername(config.dbConfig.user)
          hc
        }
        ds <- ZIO.attempt(new HikariDataSource(hikariConf))
      } yield ds
    }

  }
}
