package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NilShape, NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.ScalarShapeModel
import org.yaml.model.YMap

/** parses primitive avro types such as null, boolean, int, long, float, double, bytes, string */
case class AvroTextTypeParser(`type`: String, maybeMap: Option[YMap])(implicit
    ctx: AvroSchemaContext
) extends AvroComplexShapeParser(maybeMap.getOrElse(YMap.empty)) {
  private val avroPrimitiveTypes = Set("null", "boolean", "int", "long", "float", "double", "bytes", "string")

  private lazy val defaultAnnotations = (Annotations.virtual(), Annotations.inferred())
  private lazy val (annotations, typeAnnotations): (Annotations, Annotations) =
    maybeMap.map(annotationsFromMap).getOrElse(defaultAnnotations)

  private def annotationsFromMap(map: YMap) = (Annotations(map), Annotations(map.entries.head))

  override lazy val shape: ScalarShape = ScalarShape(annotations)

  override def parse(): AnyShape = `type` match {
    case "null" => NilShape(annotations).withName(`type`, typeAnnotations)
    case s if avroPrimitiveTypes.contains(s) =>
      shape.setWithoutId(ScalarShapeModel.DataType, AmfScalar(DataType(`type`), annotations), typeAnnotations)
    case _ if ctx.globalSpace.contains(`type`) =>
      val originalShape = ctx.globalSpace(`type`).asInstanceOf[NodeShape]
      originalShape.link(`type`, originalShape.annotations)
    case _ => shape
  }

  override def parseSpecificFields(): Unit = {}
}
