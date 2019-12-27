package amf.plugins.document.webapi.contexts.parser.async

import amf.plugins.document.webapi.contexts.SpecAwareContext
import amf.plugins.document.webapi.contexts.parser.OasLikeSpecVersionFactory
import amf.plugins.document.webapi.parser.spec.declaration.SecuritySchemeParser
import amf.plugins.document.webapi.parser.spec.domain
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Server}
import org.yaml.model.{YMapEntry, YPart}

import scala.collection.mutable.ListBuffer

// TODO ASYNC complete all this
trait AsyncSpecAwareContext extends SpecAwareContext {}

trait AsyncSpecVersionFactory extends OasLikeSpecVersionFactory {}

case class Async20VersionFactory(ctx: AsyncWebApiContext) extends AsyncSpecVersionFactory {
  override def serverVariableParser(entry: YMapEntry, server: Server): OasLikeServerVariableParser =
    domain.AsyncServerVariableParser(entry, server)(ctx)

  override def operationParser(entry: YMapEntry, producer: String => Operation): OasLikeOperationParser =
    AsyncOperationParser(entry, producer)(ctx)
  override def endPointParser(entry: YMapEntry,
                              producer: String => EndPoint,
                              collector: ListBuffer[EndPoint]): OasLikeEndpointParser =
    AsyncEndpointParser(entry, producer, collector)(ctx)

  override def securitySchemeParser: (YPart, SecurityScheme => SecurityScheme) => SecuritySchemeParser = ???
}
