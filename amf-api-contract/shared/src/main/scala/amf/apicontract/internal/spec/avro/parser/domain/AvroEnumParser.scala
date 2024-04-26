package amf.apicontract.internal.spec.avro.parser.domain

import amf.core.client.scala.model.domain.ScalarNode
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YSequence}
import amf.core.internal.parser.{Root, YMapOps, YScalarYRead}

class AvroEnumParser(map: YMap) extends AvroScalarShapeParser("string", Some(map)) {

  override def parse() = {
    val shape = super.parse()
    map.key("symbols").map(s => shape.withValues(parseSymbols(s)))
    shape
  }

  def parseSymbols(e: YMapEntry) = {
    val symbols = e.value.as[YSequence]
    symbols.nodes.map(buildDataNode).toSeq
  }

  def buildDataNode(symbol: YNode) = ScalarNode(symbol.as[YScalar].text, Some(XsdTypes.xsdString.iri()), symbol)
}
