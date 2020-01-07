package amf.plugins.document.webapi.parser.spec.domain

import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.domain.webapi.models.Server
import org.yaml.model.YMap
import amf.core.parser.{Annotations, YMapOps}
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.document.webapi.parser.spec.domain.binding.AsyncServerBindingsParser
import amf.plugins.domain.webapi.metamodel.ServerModel

case class AsyncServerParser(parent: String, map: YMap)(implicit override val ctx: AsyncWebApiContext)
    extends OasLikeServerParser(parent, map) {

  override def parse(): Server = {
    val server = super.parse()
    map.key("protocol", ServerModel.Protocol in server)
    map.key("protocolVersion", ServerModel.ProtocolVersion in server)
    map.key("bindings").foreach { entry =>
      val bindings = AsyncServerBindingsParser.parse(entry.value.as[YMap], server.id)
      server.setArray(ServerModel.Bindings, bindings, Annotations(entry))

      AnnotationParser(server, map).parseOrphanNode("bindings")
    }

    // todo complete these
    //    map.key("security", ServerModel.Security in server using SecurityRequirementParser)
    server
  }
}
