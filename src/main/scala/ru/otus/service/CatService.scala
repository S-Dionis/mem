package ru.otus.service

import spray.json.DefaultJsonProtocol._
import zio._
import zio.http.Client
import spray.json._
import zio.stream.ZSink

import java.io.{ByteArrayInputStream, File}
import javax.imageio.ImageIO

object CatService {
  private val url = "https://api.thecatapi.com/v1/images/search"

  private case class TheCatApi(id: String, url: String, width: Int, height: Int)

  private implicit val it = jsonFormat4(TheCatApi)

  type CatService = Service

  trait Service {
    def random(): ZIO[Client, Throwable, Array[Byte]]
  }

  case class ServiceImpl() extends Service {
    override def random(): ZIO[Client, Throwable, Array[Byte]] = for {
      res       <- Client.request(url)
      data      <- res.body.asString
      theCatApi <- ZIO.attempt(data.parseJson.convertTo[List[TheCatApi]])
      res1      <- Client.request(theCatApi.head.url)
      arr       <- res1.body.asArray
      //_ <- ZIO.attempt(save(arr)) //TODO сделать с закрытием ресурсов это тут
    } yield (arr)


  }

  val live = ZLayer.succeed(new ServiceImpl)

}
