package amf.plugins.document.webapi.contexts.parser.async

import amf.plugins.document.webapi.contexts.SpecAwareContext
import amf.plugins.document.webapi.contexts.parser.OasLikeSpecVersionFactory
import amf.plugins.document.webapi.parser.spec.async.parser.{AsyncOperationParser, AsyncServerVariableParser}
import amf.plugins.document.webapi.parser.spec.declaration.{
  Async2SecuritySchemeParser,
  Async2SecuritySettingsParser,
  OasLikeSecuritySettingsParser,
  SecuritySchemeParser
}
import amf.plugins.document.webapi.parser.spec.domain
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.domain.webapi.models.security.SecurityScheme
import amf.plugins.domain.webapi.models.{EndPoint, Operation}
import org.yaml.model.{YMap, YMapEntry, YPart}

// TODO ASYNC complete all this
trait AsyncSpecAwareContext extends SpecAwareContext {}

trait AsyncSpecVersionFactory extends OasLikeSpecVersionFactory {}

case class Async20VersionFactory()(implicit ctx: AsyncWebApiContext) extends AsyncSpecVersionFactory {
  override def serverVariableParser(entry: YMapEntry, parent: String): OasLikeServerVariableParser =
    AsyncServerVariableParser(entry, parent)(ctx)

  override def operationParser(entry: YMapEntry, parentId: String): OasLikeOperationParser =
    AsyncOperationParser(entry, parentId)(ctx)
  override def endPointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint]): OasLikeEndpointParser =
    AsyncEndpointParser(entry, parentId, collector)(ctx)

  override def securitySchemeParser: (YPart, SecurityScheme => SecurityScheme) => SecuritySchemeParser =
    Async2SecuritySchemeParser.apply

  override def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasLikeSecuritySettingsParser =
    new Async2SecuritySettingsParser(map, scheme)
}
