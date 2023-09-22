package ru.otus.dao.repository

import io.getquill._
import io.getquill.context.ZioJdbc.QIO
import io.getquill.jdbczio.Quill
import ru.otus.dao.entity.Photo
import ru.otus.database
import zio._

import java.sql.SQLException
import java.util.UUID

object PhotoRepository {

  val dc = database.Ctx
  import dc._

  type PhotoRepository = Service

  trait Service {
    def top(count: Int): QIO[List[String]]
    def list(): QIO[List[String]]
    def photo(id: String): QIO[Option[Photo]]
    def create(photo: Photo): QIO[Photo]
    def update(photo: Photo): QIO[Unit]
  }

  class ServiceImpl extends Service {

    private lazy val photoSchema = quote {
      querySchema[Photo](""""photo"""")
    }

    override def top(count: Int): QIO[List[String]] = run(photoSchema.sortBy(_.rating)(Ord.desc).take(lift(count)).map(_.id))

    override def photo(id: String): Result[Option[Photo]] = run(photoSchema.filter(_.id == lift(id)))
      .map(_.headOption)

    override def create(photo: Photo): Result[Photo] = run(photoSchema.insertValue(lift(photo))).as(photo)

    override def list(): Result[List[String]] = run(photoSchema.map(_.id))

    override def update(photo: Photo): Result[Unit] = run(photoSchema.filter(_.id == lift(photo.id))updateValue(lift(photo))).unit

  }

  val live = ZLayer.succeed(new ServiceImpl)

}