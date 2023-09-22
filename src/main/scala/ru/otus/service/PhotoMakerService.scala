package ru.otus.service

import ru.otus.service.util.MakePhoto
import zio._

object PhotoMakerService {

  type PhotoMakerService = Service

  trait Service {
    def makePhoto(arr: Array[Byte], metadata: String): Task[Array[Byte]]
  }

  case class ServiceImpl() extends Service{

    override def makePhoto(arr: Array[Byte], text: String): Task[Array[Byte]] = ZIO.attempt(MakePhoto.makePhoto(arr, text))

  }

  val live: ULayer[ServiceImpl] = ZLayer.succeed { ServiceImpl() }

}
