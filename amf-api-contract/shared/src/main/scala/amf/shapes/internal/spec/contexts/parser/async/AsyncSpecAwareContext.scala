package amf.shapes.internal.spec.contexts.parser.async

import amf.shapes.internal.spec.contexts.SpecAwareContext
import amf.shapes.internal.spec.contexts.parser.OasLikeSpecVersionFactory
import amf.plugins.document.apicontract.parser.spec.async.parser.{AsyncOperationParser, AsyncServerVariableParser}
import amf.plugins.document.apicontract.parser.spec.declaration.{
  Async2SecuritySchemeParser,
  Async2SecuritySettingsParser,
  OasLikeSecuritySettingsParser,
  SecuritySchemeParser
}
import amf.plugins.document.apicontract.parser.spec.domain
import amf.plugins.document.apicontract.parser.spec.domain._
import amf.plugins.domain.apicontract.models.security.SecurityScheme
import amf.plugins.domain.apicontract.models.{EndPoint, Operation}
import org.yaml.model.{YMap, YMapEntry, YPart}

// TODO ASYNC complete all this
trait AsyncSpecAwareContext extends SpecAwareContext {}

trait AsyncSpecVersionFactory extends OasLikeSpecVersionFactory {}

case class Async20VersionFactory()(implicit ctx: AsyncWebApiContext) extends AsyncSpecVersionFactory {
  override def serverVariableParser(entry: YMapEntry, parent: String): OasLikeServerVariableParser =
    AsyncServerVariableParser(entry, parent)(ctx)

  override def operationParser(entry: YMapEntry, adopt: Operation => Operation): OasLikeOperationParser =
    AsyncOperationParser(entry, adopt)(ctx)
  override def endPointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint]): OasLikeEndpointParser =
    AsyncEndpointParser(entry, parentId, collector)(ctx)

  override def securitySchemeParser: (YPart, SecurityScheme => SecurityScheme) => SecuritySchemeParser =
    Async2SecuritySchemeParser.apply

  override def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasLikeSecuritySettingsParser =
    new Async2SecuritySettingsParser(map, scheme)
}
