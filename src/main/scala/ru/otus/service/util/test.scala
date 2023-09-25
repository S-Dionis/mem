package ru.otus.service.util

import java.io.{ByteArrayInputStream, File}
import java.util.UUID
import javax.imageio.ImageIO

object test {

  def save(array: Array[Byte]): Unit = {
    val bais = new ByteArrayInputStream(array)
    val image = ImageIO.read(bais)
    val uuid = UUID.randomUUID().toString
    val saveTo = new File(s"C:\\Users\\denis\\Pictures\\$uuid.jpg")
    if (image != null)
      ImageIO.write(image, "jpg", saveTo)
    else
      println()

    bais.close()
  }

}
