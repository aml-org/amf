package amf.spec.domain

import amf.domain.{Annotations, Payload}
import amf.spec.ParserContext
import amf.spec.common.{AnnotationParser, ValueNode}
import amf.spec.declaration.RamlTypeParser
import org.yaml.model.{YMap, YMapEntry, YType}

/**
  *
  */
case class RamlPayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload)(implicit ctx: ParserContext) {
  def parse(): Payload = {

    val payload = producer(Some(ValueNode(entry.key).string().value.toString)).add(Annotations(entry))

    entry.value.to[YMap] match {
      case Right(map) =>
        // TODO
        // Should we clean the annotations here so they are not parsed again in the shape?
        AnnotationParser(() => payload, map).parse()
      case _ =>
    }

    entry.value.tag.tagType match {
      case YType.Null =>
      case _ =>
        RamlTypeParser(entry, shape => shape.withName("schema").adopted(payload.id))
          .parse()
          .foreach(payload.withSchema)

    }
    payload
  }
}
