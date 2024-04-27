package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroWebAPIContext
import amf.apicontract.internal.spec.avro.parser.domain.AvroScalarShapeParser.defaultAnnotations
import amf.core.client.scala.vocabulary.Namespace.{Xsd, XsdTypes}
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.{AnyShape, NilShape, ScalarShape}
import amf.shapes.internal.domain.parser.XsdTypeDefMapping
import org.yaml.model.{YMap, YMapEntry}
import amf.core.internal.parser.YMapOps
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps

case class AvroScalarShapeParser(`type`: String, yMap: Option[YMap])(implicit ctx: AvroWebAPIContext)
    extends AvroShapeBaseParser(yMap.getOrElse(YMap.empty)) {

  private val (annotations, typeAnnotations): (Annotations, Annotations) =
    yMap.map(annotationsFromMap).getOrElse(defaultAnnotations)

  private def annotationsFromMap(map: YMap) = (Annotations(map), Annotations(map.entries.head))

  private def nilShape    = NilShape(annotations).withName(`type`)
  private def scalarShape = ScalarShape(annotations).withName(`type`).withDataType(dataType, typeAnnotations)

  private def dataType: String = {
    // bytes is ok as xsd#bytes? or shall we use format?
    XsdTypeDefMapping.xsdFromString(`type`)._1.getOrElse(Xsd.base + `type`)
  }

  override protected def parseShape(): AnyShape = getShape()

  private def getShape() = if (`type` == "null") nilShape else scalarShape
}

object AvroScalarShapeParser {
  private val defaultAnnotations = (Annotations.virtual(), Annotations.inferred())
}
