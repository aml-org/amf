package amf.apicontract.internal.spec.async.parser

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.spec.common.emitter.SpecAwareContext
import amf.apicontract.internal.spec.common.parser.SecuritySchemeParser
import amf.apicontract.internal.spec.oas.parser._
import amf.apicontract.internal.spec.oas.parser.context.OasLikeSpecVersionFactory
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
