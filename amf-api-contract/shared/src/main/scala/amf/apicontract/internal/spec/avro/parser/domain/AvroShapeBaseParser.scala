package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.internal.parser.YMapOps
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import amf.shapes.internal.spec.common.parser.QuickFieldParserOps
import org.yaml.model.{YMap, YNode}

abstract class AvroShapeBaseParser(map: YMap)(implicit ctx: AvroSchemaContext)
    extends QuickFieldParserOps
    with AvroKeyExtractor {
  val shape: AnyShape

  def parse(): AnyShape = {
    map.key("name", AnyShapeModel.Name in shape)
    map.key("namespace", (AnyShapeModel.AvroNamespace in shape).allowingAnnotations)
    map.key("aliases", (AnyShapeModel.Aliases in shape).allowingAnnotations)
    map.key("doc", (AnyShapeModel.Description in shape).allowingAnnotations)
    parseSpecificFields()
    shape
  }

  def parseSpecificFields(): Unit = {}
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
