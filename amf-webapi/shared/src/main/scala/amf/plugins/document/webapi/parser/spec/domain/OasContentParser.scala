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
import org.yaml.model.{YMap, YMapEntry}

import scala.collection.mutable

case class OasContentsParser(entry: YMapEntry, producer: Option[String] => Payload)(implicit ctx: OasWebApiContext) {
  def parse(): List[Payload] = {
    val payloads = mutable.ListBuffer[Payload]()
    entry.value
      .as[YMap]
      .entries
      .foreach { entry =>
        payloads += OasContentParser(entry, producer)(ctx).parse()
      }
    payloads.toList
  }
}

case class OasContentParser(entry: YMapEntry, producer: Option[String] => Payload)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {

  private def buildPayload(): Payload = {
    val mediaTypeNode         = ScalarNode(entry.key)
    val mediaTypeText: String = mediaTypeNode.text().toString

    val payload = producer(Some(mediaTypeText)).add(Annotations(entry))
    payload.set(PayloadModel.MediaType, mediaTypeNode.string())
  }

  def parse(): Payload = {
    val map     = entry.value.as[YMap]
    val payload = buildPayload()

    ctx.closedShape(payload.id, map, "content")

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
        payload.mediaType.option().foreach(ex.withMediaType)
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
