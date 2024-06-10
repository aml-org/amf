package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.vocabulary.Namespace.Xsd
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NilShape, ScalarShape}
import amf.shapes.internal.domain.parser.XsdTypeDefMapping
import org.yaml.model.YMap

/** parses primitive avro types such as null, boolean, int, long, float, double, bytes */
case class AvroScalarShapeParser(`type`: String, maybeMap: Option[YMap])(implicit ctx: AvroSchemaContext)
    extends AvroShapeBaseParser(maybeMap.getOrElse(YMap.empty)) {

  private val defaultAnnotations = (Annotations.virtual(), Annotations.inferred())
  private val (annotations, typeAnnotations): (Annotations, Annotations) =
    maybeMap.map(annotationsFromMap).getOrElse(defaultAnnotations)

  private def annotationsFromMap(map: YMap) = (Annotations(map), Annotations(map.entries.head))
  private def nilShape                      = NilShape(annotations).withName(`type`)
  private def scalarShape = ScalarShape(annotations).withName(`type`).withDataType(dataType, typeAnnotations)
  private def dataType: String = {
    // todo: bytes is ok as xsd#bytes? or shall we use format?
    // todo: maybe use TypeDef? investigate map to see if it has the info inside, validate that is a valid AVRO primitive type
    XsdTypeDefMapping.xsdFromString(`type`)._1.getOrElse(Xsd.base + `type`)
  }

  override def parse(): AnyShape = if (`type` == "null") nilShape else scalarShape
}
