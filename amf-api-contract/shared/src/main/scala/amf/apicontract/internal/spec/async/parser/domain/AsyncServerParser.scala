package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.{Server, Tag}
import amf.apicontract.client.scala.model.domain.api.AsyncApi
import amf.apicontract.internal.metamodel.domain.ServerModel
import amf.apicontract.internal.spec.async.parser.bindings.AsyncServerBindingsParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorServer
import amf.apicontract.internal.spec.oas.parser.domain.{OasLikeServerParser, TagsParser}
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.core.internal.validation.CoreValidations
import amf.shapes.internal.spec.common.parser.{AnnotationParser, YMapEntryLike}
import org.yaml.model.{YMap, YNode}

abstract class AsyncServersParser(map: YMap, api: AsyncApi)(implicit val ctx: AsyncWebApiContext) {

  protected def serverParser(entryLike: YMapEntryLike): OasLikeServerParser

  def parse(): Seq[Server] = {
    map.entries.map { entry =>
      serverParser(YMapEntryLike(entry))
        .parse()
        .setWithoutId(
          ServerModel.Name,
          AmfScalar(entry.key.asScalar.map(_.text).getOrElse(entry.key.toString), Annotations(entry.key)),
          Annotations.inferred()
        )
    }
  }
}

class Async20ServersParser(map: YMap, api: AsyncApi)(
    override implicit val ctx: AsyncWebApiContext
) extends AsyncServersParser(map, api) {
  override protected def serverParser(entryLike: YMapEntryLike): OasLikeServerParser =
    new Async20ServerParser(api.id, entryLike)
}

class Async23ServersParser(map: YMap, api: AsyncApi)(
    override implicit val ctx: AsyncWebApiContext
) extends AsyncServersParser(map, api) {
  override protected def serverParser(entryLike: YMapEntryLike): OasLikeServerParser =
    new Async23ServerParser(api.id, entryLike)
}

class Async25ServersParser(map: YMap, api: AsyncApi)(override implicit val ctx: AsyncWebApiContext)
    extends AsyncServersParser(map, api) {
  override protected def serverParser(entryLike: YMapEntryLike): OasLikeServerParser =
    new Async25SeverParser(api.id, entryLike)
}

class Async20ServerParser(parent: String, entryLike: YMapEntryLike)(implicit
    override val ctx: AsyncWebApiContext
) extends OasLikeServerParser(parent, entryLike)
    with SecuritySchemeParser {

  override def parse(): Server = {
    val server = super.parse()
    map.key("protocol", ServerModel.Protocol in server)
    map.key("protocolVersion", ServerModel.ProtocolVersion in server)
    map.key("bindings").foreach { entry =>
      val bindings = AsyncServerBindingsParser(YMapEntryLike(entry.value)).parse()
      server.setWithoutId(ServerModel.Bindings, bindings, Annotations(entry))

      AnnotationParser(server, map).parseOrphanNode("bindings")
    }

    map.key(
      "security",
      entry => parseSecurityScheme(entry, ServerModel.Security, server)
    )

    server
  }
}

class Async23ServerParser(parent: String, entryLike: YMapEntryLike)(implicit override val ctx: AsyncWebApiContext)
    extends Async20ServerParser(parent, entryLike) {

  override def parse(): Server = {
    val map: YMap = entryLike.asMap
    ctx.link(map) match {
      case Left(fullRef) => handleRef(fullRef)
      case Right(_)      => super.parse()
    }
  }

  private def handleRef(fullRef: String): Server = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, "servers")
    ctx.declarations
      .findServer(label, SearchScope.Named)
      .map(server => nameAndAdopt(generateLink(label, server, entryLike), entryLike.key))
      .getOrElse(remote(fullRef, entryLike))
  }

  private def remote(fullRef: String, entryLike: YMapEntryLike)(implicit ctx: AsyncWebApiContext): Server = {
    ctx.navigateToRemoteYNode(fullRef) match {
      case Some(result) =>
        val serverNode = result.remoteNode
        val external   = new Async23ServerParser(parent, YMapEntryLike(serverNode))(result.context).parse()
        nameAndAdopt(
          external.link(AmfScalar(fullRef), entryLike.annotations, Annotations.synthesized()),
          entryLike.key
        ) // check if this link should be trimmed to just the label
      case None =>
        ctx.eh.violation(
          CoreValidations.UnresolvedReference,
          "",
          s"Cannot find link reference $fullRef",
          entryLike.asMap.location
        )
        val errorServer = ErrorServer(fullRef, entryLike.asMap)
        nameAndAdopt(errorServer.link(fullRef, errorServer.annotations), entryLike.key)
    }
  }

  private def generateLink(label: String, effectiveTarget: Server, entryLike: YMapEntryLike): Server = {
    val server = Server(entryLike.annotations)
    val hash   = s"${server.id}$label".hashCode
    server
      .withId(s"${server.id}/link-$hash")
      .withLinkTarget(effectiveTarget)
      .withLinkLabel(label, Annotations(entryLike.value))
  }

  def nameAndAdopt(s: Server, key: Option[YNode]): Server = {
    key foreach { k =>
      s.setWithoutId(ServerModel.Name, ScalarNode(k).string(), Annotations(k))
    }
    s
  }
}

class Async25SeverParser(parent: String, entryLike: YMapEntryLike)(implicit override val ctx: AsyncWebApiContext)
    extends Async23ServerParser(parent, entryLike) {
  override def parse(): Server = {
    val server = super.parse()
    map.key("tags").foreach { entry =>
      val tags = entry.value.as[Seq[YMap]].map(tag => TagsParser(tag, (tag: Tag) => tag).parse())
      server.setWithoutId(ServerModel.Tags, AmfArray(tags, Annotations(entry.value)), Annotations(entry))
    }
    server
  }
}
