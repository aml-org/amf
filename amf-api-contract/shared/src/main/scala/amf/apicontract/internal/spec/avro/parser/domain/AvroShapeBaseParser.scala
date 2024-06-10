package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.internal.parser.YMapOps
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps
import org.yaml.model.{YMap, YNode}

abstract class AvroShapeBaseParser(map: YMap)(implicit ctx: AvroSchemaContext)
    extends QuickFieldParserOps
    with AvroKeyExtractor {

  def parse(): AnyShape
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
