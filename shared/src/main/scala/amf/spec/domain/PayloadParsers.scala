package amf.spec.domain

import amf.domain.{Annotations, Payload}
import amf.spec.Declarations
import amf.spec.common.{AnnotationParser, ValueNode}
import amf.spec.declaration.RamlTypeParser
import amf.validation.Validation
import org.yaml.model.{YMap, YMapEntry, YType}

/**
  *
  */
case class RamlPayloadParser(entry: YMapEntry, producer: (Option[String]) => Payload, declarations: Declarations, currentValidation: Validation) {
  def parse(): Payload = {

    val payload = producer(Some(ValueNode(entry.key).string().value.toString)).add(Annotations(entry))

    entry.value.value match {
      case map: YMap =>
        // TODO
        // Should we clean the annotations here so they are not parsed again in the shape?
        AnnotationParser(() => payload, map).parse()
      case _ =>
    }

    entry.value.tag.tagType match {
      case YType.Null =>
      case _ =>
        RamlTypeParser(entry, shape => shape.withName("schema").adopted(payload.id), declarations, currentValidation)
          .parse()
          .foreach(payload.withSchema)

    }
    payload
  }
}