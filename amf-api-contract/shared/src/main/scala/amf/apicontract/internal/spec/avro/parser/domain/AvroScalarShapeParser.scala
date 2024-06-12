package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.DataType
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NilShape, ScalarShape}
import org.yaml.model.{YMap, YScalar}

/** parses primitive avro types such as null, boolean, int, long, float, double, bytes, string */
case class AvroScalarShapeParser(`type`: String, maybeMap: Option[YMap])(implicit ctx: AvroSchemaContext)
    extends AvroShapeBaseParser(maybeMap.getOrElse(YMap.empty)) {
  private val avroPrimitiveTypes = Set("null", "boolean", "int", "long", "float", "double", "bytes", "string")

  private val defaultAnnotations = (Annotations.virtual(), Annotations.inferred())
  private val (annotations, typeAnnotations): (Annotations, Annotations) =
    maybeMap.map(annotationsFromMap).getOrElse(defaultAnnotations)

  private def annotationsFromMap(map: YMap) = (Annotations(map), Annotations(map.entries.head))

  override val shape: ScalarShape = ScalarShape(annotations)

  override def parse(): AnyShape = `type` match {
    case "null" => NilShape(annotations).withName(`type`)
    case s if avroPrimitiveTypes.contains(s) =>
      parseCommonFields()
      shape.withDataType(DataType(`type`), typeAnnotations)
    case _ => shape
  }
}
