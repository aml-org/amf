package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroWebAPIContext
import org.yaml.model.{YMap, YMapEntry, YNode}
import amf.core.internal.parser.{Root, YMapOps, YScalarYRead}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps

abstract class AvroShapeBaseParser(map: YMap)(implicit ctx: AvroWebAPIContext)
    extends QuickFieldParserOps
    with AvroKeyExtractor {

  protected def parseShape(): AnyShape

  def parse() = {
    val shape = parseShape()
    map.key("name", (AnyShapeModel.Name in shape).allowingAnnotations)

    shape
  }

}

trait AvroKeyExtractor {
  implicit class YMapKeys(map: YMap) {
    def typeValue: Option[YNode] = map.key("type").map(_.value)
    def `type`: Option[String]   = typeValue.flatMap(_.asScalar).map(_.text)
  }

  implicit class StringAvroOps(value: String) {
    def isPrimitive: Boolean =
      Seq("null", "boolean", "int", "long", "float", "double", "bytes", "string").contains(value)
  }
}
