package amf.avro

import amf.apicontract.client.scala.{AMFConfiguration, AvroConfiguration}
import amf.cache.CustomUnitCache
import amf.core.client.scala.AMFParseResult
import amf.core.client.scala.config.CachedReference
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.shapes.client.scala.model.document.AvroSchemaDocument

import scala.concurrent.{ExecutionContext, Future}

trait AvroSchemaDocumentTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext {

  protected val basePath: String

  protected def parse(
      avroSchemaPath: String,
      apiPath: String,
      base: AMFConfiguration
  ): Future[(AMFParseResult, AvroSchemaDocument)] = {
    for {
      (config, doc) <- withAvroSchema(avroSchemaPath, base)
      client        <- Future.successful(config.baseUnitClient())
      parsed        <- client.parse(computePath(apiPath))
    } yield {
      (parsed, doc)
    }
  }

  protected def withAvroSchema(
      path: String,
      config: AMFConfiguration
  ): Future[(AMFConfiguration, AvroSchemaDocument)] = {
    val client =
      avroSchemaConfiguration().withErrorHandlerProvider(() => UnhandledErrorHandler).baseUnitClient()
    for {
      parsed <- client.parse(computePath(path)).map(_.baseUnit)
    } yield {
      val reference = CachedReference(computePath(path), parsed)
      val cache     = CustomUnitCache(Seq(reference))
      (config.withUnitCache(cache), parsed.asInstanceOf[AvroSchemaDocument])
    }
  }

  protected def computePath(ref: String): String = {
    if (basePath.startsWith("file://")) basePath + ref
    else s"file://${basePath}" + ref
  }

  protected def avroSchemaConfiguration(): AMFConfiguration = AvroConfiguration.Avro()

}
