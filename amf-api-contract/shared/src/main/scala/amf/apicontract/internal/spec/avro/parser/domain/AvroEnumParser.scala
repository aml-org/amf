package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, ScalarNode}
import amf.core.internal.metamodel.domain.ScalarNodeModel.Value
import amf.core.client.scala.model.DataType
import amf.core.internal.metamodel.domain.{ScalarNodeModel, ShapeModel}
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.{YMapOps, YScalarYRead}
import amf.shapes.client.scala.model.domain.AnyShape
import org.yaml.model._

class AvroEnumParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroTextTypeParser("string", Some(map)) {

  override def parse(): AnyShape = {
    val shape = super.parse()
    parseCommonFields()
    parseSpecificFields()
    parseDefault()
    shape
  }

  override def parseSpecificFields(): Unit = {
    map.key("symbols").map { entry =>
      val symbols = parseSymbols(entry)
      shape.setWithoutId(ShapeModel.Values, AmfArray(symbols, Annotations(entry.value)), Annotations(entry))
    }
  }

  private def parseSymbols(e: YMapEntry): Seq[ScalarNode] = {
    val symbols = e.value.as[YSequence]
    symbols.nodes.map(buildDataNode)
  }

  private def buildDataNode(symbol: YNode): ScalarNode = {
    val enum = symbol.as[YScalar].text
    val ann  = Annotations(symbol)
    ScalarNode(ann)
      .set(Value, AmfScalar(enum, ann), Annotations.inferred())
      .set(ScalarNodeModel.DataType, AmfScalar(DataType.String, Annotations.inferred()), Annotations.inferred())
  }
}
