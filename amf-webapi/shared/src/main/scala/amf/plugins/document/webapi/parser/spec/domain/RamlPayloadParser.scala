package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.parser.{Annotations, ValueNode}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.document.webapi.parser.spec.declaration.{AnyDefaultType, Raml10TypeParser}
import amf.plugins.domain.webapi.models.Payload
import org.yaml.model.{YMap, YMapEntry, YType}

/**
  *
  */
case class RamlPayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload)(implicit ctx: WebApiContext) {
  def parse(): Payload = {

    val payload = producer(Some(ValueNode(entry.key).string().value.toString)).add(Annotations(entry))

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
