package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.parser.{Annotations, ScalarNode, _}
import amf.plugins.document.apicontract.contexts.parser.oas.OasWebApiContext
import amf.plugins.document.apicontract.parser.WebApiShapeParserContextAdapter
import amf.plugins.document.apicontract.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.apicontract.parser.spec.declaration.OasTypeParser
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.apicontract.metamodel.PayloadModel
import amf.plugins.domain.apicontract.models.Payload
import org.yaml.model.{YMap, YNode}

case class OasPayloadParser(node: YNode, producer: Option[String] => Payload)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {
  def parse(): Payload = {
    val map = node.as[YMap]
    val payload = producer(
      map.key("mediaType").map(entry => ScalarNode(entry.value).text().value.toString)
    ).add(Annotations.valueNode(map))

    // todo set again for not lose annotations?

    map.key("name", PayloadModel.Name in payload)
    map.key("mediaType", PayloadModel.MediaType in payload)

    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, shape => shape.withName("schema").adopted(payload.id))(
          WebApiShapeParserContextAdapter(ctx))
          .parse()
          .map(s => payload.set(PayloadModel.Schema, tracking(s, payload.id), Annotations(entry)))
      }
    )

    AnnotationParser(payload, map)(WebApiShapeParserContextAdapter(ctx)).parse()

    payload
  }
}
