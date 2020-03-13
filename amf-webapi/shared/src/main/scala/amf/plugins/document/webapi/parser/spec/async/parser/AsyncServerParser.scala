package amf.plugins.document.webapi.parser.spec.async.parser

import amf.core.model.domain.AmfArray
import amf.core.parser._
import amf.core.parser.Annotations
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, YMapEntryLike}
import amf.plugins.document.webapi.parser.spec.domain.binding.AsyncServerBindingsParser
import amf.plugins.document.webapi.parser.spec.domain.{OasLikeSecurityRequirementParser, OasLikeServerParser}
import amf.plugins.domain.webapi.metamodel.ServerModel
import amf.plugins.domain.webapi.models.{Server, WebApi}
import org.yaml.model.{YMap, YNode}

case class AsyncServersParser(map: YMap, api: WebApi)(implicit val ctx: AsyncWebApiContext) {

  def parse(): Seq[Server] = {
    map.entries.map { entry =>
      AsyncServerParser(api.id, entry.value.as[YMap])
        .parse()
        .withName(entry.key)
    }
  }
}

private case class AsyncServerParser(parent: String, map: YMap)(implicit override val ctx: AsyncWebApiContext)
    extends OasLikeServerParser(parent, map) {

  override def parse(): Server = {
    val server = super.parse()
    map.key("protocol", ServerModel.Protocol in server)
    map.key("protocolVersion", ServerModel.ProtocolVersion in server)
    map.key("bindings").foreach { entry =>
      val bindings = AsyncServerBindingsParser(YMapEntryLike(entry.value), server.id).parse()
      server.set(ServerModel.Bindings, bindings, Annotations(entry))

      AnnotationParser(server, map).parseOrphanNode("bindings")
    }

    map.key(
      "security",
      entry => {
        val idCounter = new IdCounter()
        val securedBy = entry.value
          .as[Seq[YNode]]
          .map(s => OasLikeSecurityRequirementParser(s, server.withSecurity, idCounter).parse())
          .collect { case Some(s) => s }

        server.set(ServerModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    server
  }
}
