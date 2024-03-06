package amf.apicontract.internal.spec.async.parser.context

import amf.apicontract.client.scala.model.domain.api.AsyncApi
import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.spec.async.MessageType
import amf.apicontract.internal.spec.async.parser.domain._
import amf.apicontract.internal.spec.common.emitter.SpecAwareContext
import amf.apicontract.internal.spec.common.parser.SecuritySchemeParser
import amf.apicontract.internal.spec.oas.parser.context.OasLikeSpecVersionFactory
import amf.apicontract.internal.spec.oas.parser.domain.{
  OasLikeEndpointParser,
  OasLikeOperationParser,
  OasLikeSecuritySettingsParser,
  OasLikeServerVariableParser
}
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.{YMap, YMapEntry}

// TODO ASYNC complete all this
trait AsyncSpecAwareContext extends SpecAwareContext {}

trait AsyncSpecVersionFactory extends OasLikeSpecVersionFactory {
  def serversParser(map: YMap, api: AsyncApi): AsyncServersParser
  def messageParser(
      entryLike: YMapEntryLike,
      parent: String,
      messageType: Option[MessageType],
      isTrait: Boolean = false
  )(implicit ctx: AsyncWebApiContext): AsyncMessageParser
}

class Async20VersionFactory()(implicit ctx: AsyncWebApiContext) extends AsyncSpecVersionFactory {
  override def serverVariableParser(entry: YMapEntry, parent: String): OasLikeServerVariableParser =
    AsyncServerVariableParser(entry, parent)(ctx)
  override def operationParser(entry: YMapEntry, adopt: Operation => Operation): OasLikeOperationParser =
    AsyncOperationParser(entry, adopt)(ctx)
  override def endPointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint]): OasLikeEndpointParser =
    new Async20EndpointParser(entry, parentId, collector)(ctx)
  override def securitySchemeParser: (YMapEntryLike, SecurityScheme => SecurityScheme) => SecuritySchemeParser =
    Async2SecuritySchemeParser.apply
  override def securitySettingsParser(map: YMap, scheme: SecurityScheme): OasLikeSecuritySettingsParser =
    new Async2SecuritySettingsParser(map, scheme)
  override def serversParser(map: YMap, api: AsyncApi): AsyncServersParser = new Async20ServersParser(map, api)

  override def messageParser(
      entryLike: YMapEntryLike,
      parent: String,
      messageType: Option[MessageType],
      isTrait: Boolean = false
  )(implicit ctx: AsyncWebApiContext): AsyncMessageParser = Async20MessageParser(entryLike, parent, messageType, isTrait)
}

object Async20VersionFactory {
  def apply()(implicit ctx: AsyncWebApiContext): Async20VersionFactory = new Async20VersionFactory()(ctx)
}

class Async21VersionFactory()(implicit ctx: AsyncWebApiContext) extends Async20VersionFactory {
  override def messageParser(
      entryLike: YMapEntryLike,
      parent: String,
      messageType: Option[MessageType],
      isTrait: Boolean
  )(implicit ctx: AsyncWebApiContext): AsyncMessageParser = Async21MessageParser(entryLike, parent, messageType, isTrait)
}

object Async21VersionFactory {
  def apply()(implicit ctx: AsyncWebApiContext): Async21VersionFactory = new Async21VersionFactory()(ctx)
}

class Async22VersionFactory()(implicit ctx: AsyncWebApiContext) extends Async20VersionFactory {
  override def endPointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint]): OasLikeEndpointParser =
    new Async22EndpointParser(entry, parentId, collector)(ctx)
}

object Async22VersionFactory {
  def apply()(implicit ctx: AsyncWebApiContext): Async22VersionFactory = new Async22VersionFactory()(ctx)
}

class Async23VersionFactory()(implicit ctx: AsyncWebApiContext) extends Async21VersionFactory {
  override def endPointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint]): OasLikeEndpointParser =
    new Async23EndpointParser(entry, parentId, collector)(ctx)
  override def serversParser(map: YMap, api: AsyncApi): AsyncServersParser = new Async23ServersParser(map, api)
}

object Async23VersionFactory {
  def apply()(implicit ctx: AsyncWebApiContext): Async23VersionFactory = new Async23VersionFactory()(ctx)
}

class Async24VersionFactory()(implicit  ctx: AsyncWebApiContext) extends Async23VersionFactory{
  override def messageParser(
      entryLike: YMapEntryLike,
      parent: String,
      messageType: Option[MessageType],
      isTrait: Boolean
  )(implicit ctx: AsyncWebApiContext): AsyncMessageParser = Async24MessageParser(entryLike, parent, messageType, isTrait)
}

object Async24VersionFactory {
  def apply()(implicit ctx: AsyncWebApiContext): Async24VersionFactory = new Async24VersionFactory()(ctx)
}

class Async25VersionFactory(implicit  ctx: AsyncWebApiContext) extends Async24VersionFactory {
  override def serversParser(map: YMap, api: AsyncApi): AsyncServersParser = new Async25ServersParser(map, api)
}

object Async25VersionFactory {
  def apply()(implicit ctx: AsyncWebApiContext): Async25VersionFactory = new Async25VersionFactory()(ctx)
}
