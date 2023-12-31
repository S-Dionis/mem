package ru.otus

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource._
import ru.otus.dao.repository.PhotoRepository
import ru.otus.service.{CatService, PhotoMakerService, PhotoService}
import zio.config.getConfig
import zio.http.Header.{AccessControlAllowMethods, AccessControlAllowOrigin, Origin}
import zio.http._
import zio.http.HttpAppMiddleware.cors
import zio.http.internal.middlewares.Cors.CorsConfig
import zio.{ZIO, _}

import java.sql.DriverManager

object Main extends ZIOAppDefault {

  val config: CorsConfig =
    CorsConfig(
      allowedOrigin = {
        case origin@Origin.Value(_, host, _) if host == "localhost" =>
          Some(AccessControlAllowOrigin.Specific(origin))
        case _ => None
      },
    )

  private val server = Server.serve(ru.otus.endpoint.app @@ cors(config) @@ RequestHandlerMiddlewares.debug)

  private val migration: ZIO[configuration.Config, Throwable, Unit] = ZIO.scoped {
    for {
      _    <- Console.printLine("run migration")
      conf <- getConfig[configuration.Config]
      conn = ZIO.attempt(new JdbcConnection(DriverManager.getConnection(conf.dbConfig.url, conf.dbConfig.user, conf.dbConfig.password)))
      dbConn <- ZIO.acquireRelease(conn)(c => ZIO.succeed(c.close()))
      _ <- ZIO.attempt(
        new Liquibase(
          conf.dbConfig.migrationFile,
          new ClassLoaderResourceAccessor(getClass.getClassLoader),
          DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbConn)
        ).update("dev")
      )
      _ <- Console.printLine("migration finished")
    } yield ()
  }

  private val app = migration zipPar server

  override def run: ZIO[Any, Any, Any] = {
    //    httpProgram.provide(Server.defaultWithPort(8080))
    //    httpProgram.provide(Server.defaultWith(it => it.port(8080).enableRequestStreaming))
    app.provide(
      configuration.layer,
      PhotoService.live,
      database.Datasource.datasource,
      PhotoRepository.live,
      CatService.live,
      Client.default,
      PhotoMakerService.live,
      Server.defaultWith(it => it.port(8080).enableRequestStreaming)
    )
  }
}
