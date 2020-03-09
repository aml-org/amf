package amf.plugins.document.webapi.parser.spec.domain

import amf.core.model.domain.AmfArray
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.domain.webapi.models.Server
import org.yaml.model.{YMap, YNode}
import amf.core.parser.{Annotations, YMapOps}
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, YMapEntryLike}
import amf.plugins.document.webapi.parser.spec.domain.binding.AsyncServerBindingsParser
import amf.plugins.domain.webapi.metamodel.ServerModel

case class AsyncServerParser(parent: String, map: YMap)(implicit override val ctx: AsyncWebApiContext)
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
