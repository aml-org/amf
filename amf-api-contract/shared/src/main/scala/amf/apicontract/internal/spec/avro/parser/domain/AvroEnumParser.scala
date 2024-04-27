package amf.apicontract.internal.spec.avro.parser.domain

import amf.apicontract.internal.spec.avro.parser.context.AvroWebAPIContext
import amf.core.client.scala.model.domain.ScalarNode
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YSequence}
import amf.core.internal.parser.{Root, YMapOps, YScalarYRead}

class AvroEnumParser(map: YMap)(implicit ctx: AvroWebAPIContext) extends AvroScalarShapeParser("string", Some(map)) {

  override def parseShape() = {
    val shape = super.parseShape()
    map
      .key("symbols")
      .map(e => {
        shape.withValues(parseSymbols(e))
      })

    shape
  }

  def parseSymbols(e: YMapEntry) = {
    val symbols = e.value.as[YSequence]
    symbols.nodes.map(buildDataNode).toSeq
  }

  def buildDataNode(symbol: YNode) = ScalarNode(symbol.as[YScalar].text, Some(XsdTypes.xsdString.iri()), symbol)
}
