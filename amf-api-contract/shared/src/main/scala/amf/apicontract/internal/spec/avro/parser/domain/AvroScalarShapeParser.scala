package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.DataType
import amf.core.client.scala.vocabulary.Namespace.Xsd
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NilShape, ScalarShape}
import amf.shapes.internal.domain.parser.XsdTypeDefMapping
import org.yaml.model.YMap

/** parses primitive avro types such as null, boolean, int, long, float, double, bytes, string */
case class AvroScalarShapeParser(`type`: String, maybeMap: Option[YMap])(implicit ctx: AvroSchemaContext)
    extends AvroShapeBaseParser(maybeMap.getOrElse(YMap.empty)) {
  private val avroPrimitiveTypes = Set("null", "boolean", "int", "long", "float", "double", "bytes", "string")

  private val defaultAnnotations = (Annotations.virtual(), Annotations.inferred())
  private val (annotations, typeAnnotations): (Annotations, Annotations) =
    maybeMap.map(annotationsFromMap).getOrElse(defaultAnnotations)

  private def annotationsFromMap(map: YMap) = (Annotations(map), Annotations(map.entries.head))

  override def parse(): AnyShape = `type` match {
    case "null" => NilShape(annotations).withName(`type`)
    case s if avroPrimitiveTypes.contains(s) =>
      ScalarShape(annotations).withName(`type`).withDataType(DataType(`type`), typeAnnotations)
    case _ => AnyShape(annotations)
  }
}
