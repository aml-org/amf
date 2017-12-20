package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, ValueNode}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.document.webapi.parser.spec.declaration.{AnyDefaultType, Raml08TypeParser, Raml10TypeParser}
import amf.plugins.domain.shapes.models.NodeShape
import amf.plugins.domain.webapi.models.Payload
import org.yaml.model._
import amf.core.parser.YMapOps

/**
  *
  */
case class Raml10PayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload)(implicit ctx: WebApiContext)
    extends RamlPayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload) {

  override def parse(): Payload = {
    val payload = super.parse()

    entry.value.to[YMap] match {
      case Right(map) =>
        // TODO
        // Should we clean the annotations here so they are not parsed again in the shape?
        AnnotationParser(() => payload, map).parse()
      case _ =>
    }

    entry.value.tagType match {
      case YType.Null =>
        Raml10TypeParser(entry,
                         shape => shape.withName("schema").adopted(payload.id),
                         isAnnotation = false,
                         AnyDefaultType)
          .parse()
          .foreach { schema =>
            schema.annotations += SynthesizedField()
            payload.withSchema(schema)
          }
      case _ =>
        Raml10TypeParser(entry,
                         shape => shape.withName("schema").adopted(payload.id),
                         isAnnotation = false,
                         AnyDefaultType)
          .parse()
          .foreach(payload.withSchema)

    }

    payload
  }
}

case class Raml08PayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload)(implicit ctx: WebApiContext)
    extends RamlPayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload) {

  override def parse(): Payload = {
    val payload = super.parse()

    if (List("application/x-www-form-urlencoded", "multipart/form-data").contains(entry.key.as[YScalar].text)) {
      Raml08WebFormParser(entry.value.as[YMap], payload.id).parse().foreach(payload.withSchema)
    } else {}

    payload
  }

}

case class Raml08WebFormParser(map: YMap, parentId: String)(implicit ctx: WebApiContext) {
  def parse(): Option[NodeShape] = {
    map
      .key("formParameters")
      .flatMap(entry => {
        val entries = entry.value.as[YMap].entries
        entries.headOption.map { a =>
          val webFormShape = NodeShape(entry.value).withName("schema").adopted(parentId)

          entries.foreach(e => {

            Raml08TypeParser(e, e.key.as[YScalar].toString(), e.value, (shape: Shape) => shape)
              .parse()
              .foreach(s => {
                val property = webFormShape.withProperty(s.name)
                property.withRange(s).adopted(property.id)
              })
          })
          webFormShape
        }
      })
  }
}
abstract class RamlPayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload)(implicit ctx: WebApiContext) {
  def parse(): Payload = {

    val payload = producer(Some(ValueNode(entry.key).string().value.toString)).add(Annotations(entry))

    payload
  }
}
