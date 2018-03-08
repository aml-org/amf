package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, ScalarNode, _}
import amf.plugins.document.webapi.contexts.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeParser
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.models.Payload
import org.yaml.model.YMap

case class OasPayloadParser(map: YMap, producer: (Option[String]) => Payload)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {
  def parse(): Payload = {

    val payload = producer(
      map.key("mediaType").map(entry => ScalarNode(entry.value).text().value.toString)
    ).add(Annotations(map))

    // todo set again for not lose annotations?

    map.key("mediaType", PayloadModel.MediaType in payload)

    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, (shape) => shape.withName("schema").adopted(payload.id))
          .parse()
          .map(payload.set(PayloadModel.Schema, _, Annotations(entry)))
      }
    )

    AnnotationParser(payload, map).parse()

    payload
  }
}
