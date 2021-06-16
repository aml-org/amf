package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.Server
import amf.apicontract.client.scala.model.domain.api.AsyncApi
import amf.apicontract.client.scala.model.domain.security.SecurityRequirement
import amf.apicontract.internal.metamodel.domain.ServerModel
import amf.apicontract.internal.spec.async.parser.bindings.AsyncServerBindingsParser
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.parser.{OasLikeSecurityRequirementParser, WebApiShapeParserContextAdapter}
import amf.apicontract.internal.spec.oas.parser.OasLikeServerParser
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.IdCounter
import amf.shapes.internal.spec.common.parser.{AnnotationParser, YMapEntryLike}
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
