package amf.jsonschema

import amf.apicontract.client.scala.{AMFConfiguration, OASConfiguration}
import amf.cache.CustomUnitCache
import amf.core.client.scala.config.CachedReference
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.shapes.client.scala.config.JsonSchemaConfiguration
import amf.shapes.client.scala.model.document.JsonSchemaDocument

import scala.concurrent.{ExecutionContext, Future}

trait JsonSchemaDocumentTest {

  protected implicit def executionContext: ExecutionContext
  protected val basePath: String

  protected def parse(jsonSchemaPath: String, apiPath: String, base: AMFConfiguration) = {
    for {
      (config, doc) <- withJsonSchema(jsonSchemaPath, base)
      client        <- Future.successful(config.baseUnitClient())
      parsed        <- client.parse(computePath(apiPath))
    } yield {
      (parsed, doc)
    }
  }

  protected def withJsonSchema(
      path: String,
      config: AMFConfiguration
  ): Future[(AMFConfiguration, JsonSchemaDocument)] = {
    val client =
      JsonSchemaConfiguration.JsonSchema().withErrorHandlerProvider(() => UnhandledErrorHandler).baseUnitClient()
    for {
      parsed <- client.parse(computePath(path)).map(_.baseUnit)
    } yield {
      val reference = CachedReference(computePath(path), parsed)
      val cache     = CustomUnitCache(Seq(reference))
      (config.withUnitCache(cache), parsed.asInstanceOf[JsonSchemaDocument])
    }
  }

  protected def computePath(ref: String) = {
    if (basePath.startsWith("file://")) basePath + ref
    else s"file://${basePath}" + ref
  }
}
