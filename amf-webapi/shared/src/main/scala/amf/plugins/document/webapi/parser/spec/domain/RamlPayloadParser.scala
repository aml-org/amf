package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, YMapOps}
import amf.plugins.document.webapi.contexts.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.document.webapi.parser.spec.declaration.{AnyDefaultType, Raml08TypeParser, Raml10TypeParser}
import amf.plugins.domain.shapes.models.NodeShape
import amf.plugins.domain.webapi.metamodel.PayloadModel
import amf.plugins.domain.webapi.models.Payload
import org.yaml.model._

/**
  *
  */
case class Raml10PayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload)(
    implicit ctx: RamlWebApiContext)
    extends RamlPayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload) {

  override def parse(): Payload = {
    val payload = super.parse()

    entry.value.tagType match {
      case YType.Map => // ignore, in this case it will be parsed in the shape
      case _ =>
        entry.value.to[YMap] match {
          case Right(map) => AnnotationParser(payload, map).parse()
          case _          =>
        }
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

case class Raml08PayloadParser(entry: YMapEntry,
                               producer: (Option[String]) => Payload,
                               parseOptional: Boolean = false)(implicit ctx: RamlWebApiContext)
    extends RamlPayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload, parseOptional) {

  override def parse(): Payload = {
    val payload = super.parse()

    if (parseOptional && payload.mediaType.endsWith("?")) {
      payload.set(PayloadModel.Optional, value = true)
      payload.set(PayloadModel.MediaType, payload.mediaType.stripSuffix("?"))
    }

    entry.value.tagType match {
      case YType.Null => // Nothing
      case _ =>
        if (List("application/x-www-form-urlencoded", "multipart/form-data").contains(payload.mediaType)) {
          Raml08WebFormParser(entry.value.as[YMap], payload.id).parse().foreach(payload.withSchema)
        } else {
          Raml08TypeParser(entry, entry.key, entry.value, (shape: Shape) => shape.adopted(payload.id))
            .parse()
            .foreach(payload.withSchema)
        }
    }

    payload
  }

}

case class Raml08WebFormParser(map: YMap, parentId: String)(implicit ctx: RamlWebApiContext) {
  def parse(): Option[NodeShape] = {
    map
      .key("formParameters")
      .flatMap(entry => {
        val entries = entry.value.as[YMap].entries
        entries.headOption.map {
          _ =>
            val webFormShape = NodeShape(entry.value).withName("schema").adopted(parentId)

            entries.foreach(e => {

              Raml08TypeParser(e, e.key.as[YScalar].toString(), e.value, (shape: Shape) => shape)
                .parse()
                .foreach(s => {
                  val property = webFormShape.withProperty(s.name)
                  s.fields.entry(ShapeModel.RequiredShape) match {
                    case None                        => property.withMinCount(0)
                    case Some(f) if !f.scalar.toBool => property.withMinCount(0)
                    case _                           => property.withMinCount(1)
                  }
                  property.withRange(s).adopted(property.id)
                })
            })
            webFormShape
        }
      })
  }
}

abstract class RamlPayloadParser(entry: YMapEntry,
                                 producer: (Option[String]) => Payload,
                                 parseOptional: Boolean = false)(implicit ctx: RamlWebApiContext) {

  def parse(): Payload = producer(Some(entry.key)).add(Annotations(entry))
}
