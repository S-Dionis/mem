package ru.otus.service.util

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, IOException}
import java.awt.{Color, Font, Graphics2D}
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import scala.annotation.tailrec

object MakePhoto {

  def makePhoto(photo: Array[Byte], message: String): Array[Byte] = {
    val byteIs = new ByteArrayInputStream(photo)
    val image = ImageIO.read(byteIs)

    val nPhoto = generateNewPhoto(image, message)
    byteIs.close()
    nPhoto
  }

  private def generateNewPhoto(imageOriginal: BufferedImage, message: String): Array[Byte] = {
    val g2dOriginal = imageOriginal.createGraphics()

    val fontSize = imageOriginal.getWidth / 20 //approximately size of font
    val font = new Font(Font.MONOSPACED, Font.BOLD, fontSize)
    val fontMetrics = g2dOriginal.getFontMetrics(font)
    val messageWidth = fontMetrics.stringWidth(message) //approximately width of message
    val oneRowHeight = fontMetrics.getHeight //px
    val oneSymbolLen = messageWidth / message.length
    val indent = oneSymbolLen * 2
    val usableImageWidth = imageOriginal.getWidth - (indent * 2) // minus 2 letters each side
    val div = Math.ceil(usableImageWidth / oneSymbolLen).toInt
    val splitCoefficient = message.length / div
    val messageByParts: Array[String] = toParts(message, div)
    val messageSummaryHeight = oneRowHeight * splitCoefficient + indent * 2
    val newImage = new BufferedImage(imageOriginal.getWidth, imageOriginal.getHeight + messageSummaryHeight, imageOriginal.getType)
    val g2dNew: Graphics2D = newImage.createGraphics()

    g2dNew.setColor(Color.WHITE)
    g2dNew.fillRect(0, 0, newImage.getWidth, newImage.getHeight)
    g2dNew.drawImage(imageOriginal, 0, messageSummaryHeight, null)

    writeOnImage(messageByParts, g2dNew, indent, font)

    val outputStream = new ByteArrayOutputStream()
    ImageIO.write(newImage, "png", outputStream)
    g2dNew.dispose()
    g2dOriginal.dispose()
    val result = outputStream.toByteArray
    outputStream.close()
    result
  }

  def writeOnImage(
                    arr: Array[String],
                    graphics2D: Graphics2D,
                    indent: Int,
                    font: Font,
                    color: Color = Color.BLACK
                  ): Unit = {
    val originalFont = graphics2D.getFont
    val originalColor = graphics2D.getColor
    graphics2D.setFont(font)
    graphics2D.setColor(color)

    for {i <- arr.indices} {
      val str = arr(i)
      graphics2D.drawString(str, indent, indent + i * font.getSize)
    }

    graphics2D.setFont(originalFont)
    graphics2D.setColor(originalColor)
  }

  @tailrec
  def toParts(message: String, part: Int, arr: Array[String] = Array.empty): Array[String] = {

    if (part <= message.length) {
      if (message(part) == ' ') {
        toParts(message.substring(part).trim, part, arr :+ message.substring(0, part))
      }
      else {
        val lastSpace = message.substring(0, part).lastIndexOf(' ')
        toParts(message.substring(lastSpace).trim, part, arr :+ message.substring(0, lastSpace))
      }
    } else {
      arr :+ message
    }
  }
}
