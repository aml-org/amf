package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroSchemaContext
import amf.core.client.scala.model.domain.ScalarNode
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import amf.core.internal.parser.{YMapOps, YScalarYRead}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.AnyShapeModel
import org.yaml.model._

class AvroEnumParser(map: YMap)(implicit ctx: AvroSchemaContext) extends AvroScalarShapeParser("string", Some(map)) {

  override def parse(): AnyShape = {
    val shape = super.parse()
    map.key("name", (AnyShapeModel.Name in shape).allowingAnnotations)
    map.key("namespace", (AnyShapeModel.AvroNamespace in shape).allowingAnnotations)
    map.key("aliases", (AnyShapeModel.Aliases in shape).allowingAnnotations)
    map.key("doc", (AnyShapeModel.Description in shape).allowingAnnotations)
    map
      .key("symbols")
      .map(parseSymbols)
      .map(shape.withValues)

    // todo: parse default
    shape
  }

  private def parseSymbols(e: YMapEntry): Seq[ScalarNode] = {
    val symbols = e.value.as[YSequence]
    symbols.nodes.map(buildDataNode)
  }

  private def buildDataNode(symbol: YNode) = ScalarNode(symbol.as[YScalar].text, Some(XsdTypes.xsdString.iri()), symbol)
}
