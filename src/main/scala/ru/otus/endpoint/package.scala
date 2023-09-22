package ru.otus

import ru.otus.service.PhotoMakerService.PhotoMakerService
import ru.otus.service.PhotoService.PhotoService
import zio._
import zio.http._


import java.net.URLDecoder
import java.nio.charset.StandardCharsets

package object endpoint {

  val app = Http.collectZIO[Request] {
    case req @ Method.POST -> Root / "photo" / message =>
      (for {
        arr     <- req.body.asArray
        service <- ZIO.service[PhotoService]
        msg = URLDecoder.decode(message, StandardCharsets.UTF_8.name())
        _       <- Console.printLine(s"add photo with message $msg")
        create  <- service.insert(arr, msg)
      } yield create.id)
        .foldZIO(
          err =>
            for {
              _ <- Console.printLine(err).orDie
              r <- ZIO.succeed(Response.status(Status.BadRequest))
            } yield (r),
          result => ZIO.succeed(Response.text(result))
        )

    case req@Method.GET -> Root / "photo" / "random" =>
      (for {
        service <- ZIO.service[PhotoService]
        id <- service.photoRandom()
      } yield id).foldZIO(
        err =>
          for {
            _ <- Console.printLine(err).orDie
            r <- ZIO.succeed(Response.status(Status.BadRequest))
          } yield r,
        result => ZIO.succeed(Response.text(result))
      )

    case req@Method.GET -> Root / "photo" / "from" / id / message =>
      (for {
        service <- ZIO.service[PhotoService]
        msg = URLDecoder.decode(message, StandardCharsets.UTF_8.name())
        _ <- Console.printLine(s"New photo from $id with message $msg")
        photo <- service.photo(id)
        create  <- service.insert(photo.photo, msg)
      } yield create.id)
        .foldZIO(
          err =>
            for {
              _ <- Console.printLine(err).orDie
              r <- ZIO.succeed(Response.status(Status.BadRequest))
            } yield r,
          result => ZIO.succeed(Response.text(result))
        )

    case req@Method.GET -> Root / "photo" / "like" / id =>
      (for {
        _                 <- Console.printLine(s"Like photo with id $id")
        service <- ZIO.service[PhotoService]
        _ <- service.like(id)
      } yield ()).foldZIO(
        err =>
          for {
            _ <- Console.printLine(err).orDie
            r <- ZIO.succeed(Response.status(Status.BadRequest))
          } yield r,
        _ => ZIO.succeed(Response.status(Status.Ok))
      )

    case req @ Method.GET -> Root / "photo" / id =>
      (for {
        _                 <- Console.printLine(s"ask for photo with id $id")
        photoService      <- ZIO.service[PhotoService]
        photo             <- photoService.photo(id)
        photoMakerService <- ZIO.service[PhotoMakerService]
        arr               <- photoMakerService.makePhoto(photo.photo, photo.message)
      } yield arr).foldZIO(
        err =>
          for {
            _ <- Console.printLine(err).orDie
            r <- ZIO.succeed(Response.status(Status.BadRequest))
          } yield r,
        result =>
          for {
            _ <- ZIO.succeed()
            body = Body.fromChunk(Chunk.fromArray(result))
            r <- ZIO.succeed(Response(body = body, status = Status.Ok))
          } yield r
      )

    case req@Method.GET -> Root / "top" / count =>
      (for {
        photoService <- ZIO.service[PhotoService]
        ids <- photoService.top(count.toInt)
      } yield ids).foldZIO(
        err =>
          for {
            _ <- Console.printLine(err).orDie
            r <- ZIO.succeed(Response.status(Status.BadRequest))
          } yield r,
        result => ZIO.succeed(Response(status = Status.Ok, body = Body.fromString(result.mkString(","))))
      )

  }
}
