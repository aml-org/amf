package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.TrackedElement
import amf.core.model.domain.AmfArray
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeParser
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.models.Payload
import org.yaml.model.{YMap, YMapEntry, YNode}

import scala.collection.mutable

case class OasContentsParser(entry: YMapEntry, producer: Option[String] => Payload)(implicit ctx: OasWebApiContext) {
  def parse(): List[Payload] = {
    val payloads = mutable.ListBuffer[Payload]()
    entry.value
      .as[YMap]
      .entries
      .foreach { entry =>
        val mediaType = ScalarNode(entry.key).text().value.toString
        payloads += OasContentParser(entry.value, mediaType, producer)(ctx).parse()
      }
    payloads.toList
  }
}

case class OasContentParser(node: YNode, mediaType: String, producer: Option[String] => Payload)(
    implicit ctx: OasWebApiContext)
    extends SpecParserOps {

  def parse(): Payload = {
    val map     = node.as[YMap]
    val payload = producer(Some(mediaType)).add(Annotations.valueNode(map))

    // schema
    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, shape => shape.withName("schema").adopted(payload.id))
          .parse()
          .map(s => payload.set(PayloadModel.Schema, tracking(s, payload.id), Annotations(entry)))
      }
    )
    val examples: Seq[Example] = OasExamplesParser(map, payload.id).parse()
    if (examples.nonEmpty) {
      examples.foreach { ex =>
        ex.withMediaType(mediaType)
        ex.annotations += TrackedElement(payload.id)
      }
      payload.set(PayloadModel.Examples, AmfArray(examples))
    }

    // encoding
    map.key(
      "encoding",
      entry => {
        val encodings = OasEncodingParser(entry.value.as[YMap], payload.withEncoding).parse()
        payload.setArray(PayloadModel.Encoding, encodings, Annotations(entry))
      }
    )

    AnnotationParser(payload, map).parse()

    ctx.closedShape(payload.id, map, "content")

    payload
  }

}
