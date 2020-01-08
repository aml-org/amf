package amf.plugins.document.webapi.contexts.parser.async

import amf.plugins.document.webapi.contexts.SpecAwareContext
import amf.plugins.document.webapi.contexts.parser.OasLikeSpecVersionFactory
import amf.plugins.document.webapi.parser.spec.declaration.{
  OasLikeSecuritySettingsParser,
  Async2SecuritySchemeParser,
  SecuritySchemeParser,
  Async2SecuritySettingsParser
}
import amf.plugins.document.webapi.parser.spec.domain
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.{Server, EndPoint, Operation}
import org.yaml.model.{YMap, YPart, YMapEntry}

import scala.collection.mutable.ListBuffer

// TODO ASYNC complete all this
trait AsyncSpecAwareContext extends SpecAwareContext {}

trait AsyncSpecVersionFactory extends OasLikeSpecVersionFactory {}

case class Async20VersionFactory()(implicit ctx: AsyncWebApiContext) extends AsyncSpecVersionFactory {
  override def serverVariableParser(entry: YMapEntry, parent: String): OasLikeServerVariableParser =
    domain.AsyncServerVariableParser(entry, parent)(ctx)

  override def operationParser(entry: YMapEntry, producer: String => Operation): OasLikeOperationParser =
    AsyncOperationParser(entry, producer)(ctx)
  override def endPointParser(entry: YMapEntry,
                              producer: String => EndPoint,
                              collector: ListBuffer[EndPoint]): OasLikeEndpointParser =
    AsyncEndpointParser(entry, producer, collector)(ctx)

  override def securitySchemeParser: (YPart, SecurityScheme => SecurityScheme) => SecuritySchemeParser =
    Async2SecuritySchemeParser.apply

  override def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasLikeSecuritySettingsParser =
    new Async2SecuritySettingsParser(map, scheme)
}
