package amf.shapes.internal.annotations

import amf.core.client.scala.model.domain._
import org.yaml.model.YMapEntry

case class InlineDefinition() extends Annotation

case class ParsedJSONSchema(rawText: String) extends EternalSerializedAnnotation {
  override val name: String  = "parsed-json-schema"
  override val value: String = rawText
}

object ParsedJSONSchema extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ParsedJSONSchema(value))
}

/** Represents parsed RAML Data Type from any type of RAML document. */
case class ParsedRamlDatatype(rawText: String) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String  = "parsed-raml-datatype"
  override val value: String = rawText
}

object ParsedRamlDatatype extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ParsedRamlDatatype(value))
}

case class ParsedJSONExample(rawText: String) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String  = "parsed-json-example"
  override val value: String = rawText
}

object ParsedJSONExample extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] =
    Some(ParsedJSONExample(value))
}

case class SchemaIsJsonSchema() extends Annotation

case class ExternalSchemaWrapper() extends Annotation

case class GeneratedJSONSchema(rawText: String) extends Annotation

/** Represents generated RAML Data Type. */
case class GeneratedRamlDatatype(rawText: String) extends Annotation

/** Mark the declaration as the root of the JSON schema. */
case class JSONSchemaRoot() extends Annotation

case class JSONSchemaId(id: String) extends SerializableAnnotation with PerpetualAnnotation {
  override val name: String  = "json-schema-id"
  override val value: String = id
}

object JSONSchemaId extends AnnotationGraphLoader {
  override def unparse(value: String, objects: Map[String, AmfElement]): Option[Annotation] = Some(JSONSchemaId(value))
}

case class ExternalJsonSchemaShape(original: YMapEntry) extends Annotation

// used internally for emission of links that have been inlined. This annotation is removed in resolution
case class ExternalReferenceUrl(url: String) extends Annotation

case class CollectionFormatFromItems() extends Annotation

case class ForceEntry() extends Annotation

case class BooleanSchema() extends Annotation
