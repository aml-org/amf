package amf.plugins.document.apicontract.parser.spec.async.parser

import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.IdCounter
import amf.plugins.document.apicontract.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.common.AnnotationParser
import amf.plugins.document.apicontract.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.document.apicontract.parser.spec.domain.{OasLikeSecurityRequirementParser, OasLikeServerParser}
import amf.plugins.document.apicontract.parser.spec.domain.binding.AsyncServerBindingsParser
import amf.plugins.domain.apicontract.metamodel.ServerModel
import amf.plugins.domain.apicontract.models.Server
import amf.plugins.domain.apicontract.models.api.AsyncApi
import amf.plugins.domain.apicontract.models.security.SecurityRequirement
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

      AnnotationParser(server, map)(WebApiShapeParserContextAdapter(ctx)).parseOrphanNode("bindings")
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
