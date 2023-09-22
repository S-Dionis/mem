package ru.otus

import zio._
import com.typesafe.config.ConfigFactory
import zio.config.ReadError
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfig


package object configuration {

  case class Config(dbConfig: DbConfig)

  case class DbConfig(driver: String, url: String, user: String, password: String, migrationFile: String, schema: String)

  private val configDescriptor = descriptor[Config]

  val layer: Layer[ReadError[String], Config] = TypesafeConfig.fromTypesafeConfig(ZIO.attempt(ConfigFactory.load.resolve), configDescriptor)

}
