package ru.otus.service

import ru.otus.dao.entity.Photo
import ru.otus.dao.repository.PhotoRepository
import ru.otus.dao.repository.PhotoRepository.PhotoRepository
import ru.otus.database
import zio.CanFail.canFailAmbiguous1
import zio._
import zio.test.Gen

import java.util.UUID
import javax.sql.DataSource

object PhotoService {

  type PhotoService = Service

  trait Service {

    def insert(arr: Array[Byte], message: String): RIO[DataSource, Photo]

    def photo(id: String): RIO[DataSource, Photo]

    def photoRandom(): RIO[DataSource, String]

    def top(count: Int): RIO[DataSource, List[String]]

    def like(id: String): RIO[DataSource, Unit]
  }

  case class ServiceImpl(photoRepo: PhotoRepository.Service) extends Service {
    override def photo(id: String): RIO[DataSource, Photo] = for {
      photo <- photoRepo.photo(id).some.mapError(_ => new Exception("Failed to find photos"))
    } yield photo

    override def photoRandom(): RIO[DataSource, String] = for {
      photos <- photoRepo.list()
      rnd <- Random.nextIntBetween(0, photos.length)
      photo <- ZIO.attempt(photos(rnd))
    } yield photo

    override def top(count: Int): RIO[DataSource, List[String]] = photoRepo.top(count)

    override def like(id: String): RIO[DataSource, Unit] = for {
      photo <- photoRepo.photo(id).some.mapError(_ => new Exception("Photo not found"))
      nRating = photo.rating + 1
      _ <- photoRepo.update(photo.copy(rating = nRating))
    } yield ()

    override def insert(arr: Array[Byte], message: String): RIO[DataSource, Photo] = photoRepo.create(Photo(UUID.randomUUID().toString, 0, arr, message))
  }

  val live: URLayer[PhotoRepository, ServiceImpl] = ZLayer { for {
    photoRepo <- ZIO.service[PhotoRepository]
  } yield ServiceImpl(photoRepo)}

}
