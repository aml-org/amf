package amf.plugins.document.webapi.parser.spec.async.parser

import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser._
import amf.core.parser.Annotations
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, YMapEntryLike}
import amf.plugins.document.webapi.parser.spec.domain.binding.AsyncServerBindingsParser
import amf.plugins.document.webapi.parser.spec.domain.{OasLikeSecurityRequirementParser, OasLikeServerParser}
import amf.plugins.domain.webapi.metamodel.ServerModel
import amf.plugins.domain.webapi.models.security.SecurityRequirement
import amf.plugins.domain.webapi.models.api.AsyncApi
import amf.plugins.domain.webapi.models.Server
import org.yaml.model.{YMap, YMapEntry, YNode}

case class AsyncServersParser(map: YMap, api: AsyncApi)(implicit val ctx: AsyncWebApiContext) {

  def parse(): Seq[Server] = {
    map.entries.map { entry =>
      AsyncServerParser(api.id, entry)
        .parse()
        .set(ServerModel.Name,
             AmfScalar(entry.key.asScalar.map(_.text).getOrElse(entry.key.toString), Annotations(entry.key)),
             Annotations.inferred())
    }
  }
}

private case class AsyncServerParser(parent: String, entry: YMapEntry)(implicit override val ctx: AsyncWebApiContext)
    extends OasLikeServerParser(parent, YMapEntryLike(entry)) {

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
          .flatMap(s =>
            OasLikeSecurityRequirementParser(s, (se: SecurityRequirement) => se.adopted(server.id), idCounter).parse())

        server.set(ServerModel.Security, AmfArray(securedBy, Annotations(entry.value)), Annotations(entry))
      }
    )

    server
  }
}
