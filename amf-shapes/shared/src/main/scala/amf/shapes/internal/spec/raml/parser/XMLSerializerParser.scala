package amf.shapes.internal.spec.raml.parser

import amf.core.internal.annotations.ExplicitField
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.shapes.client.scala.domain.models.XMLSerializer
import amf.shapes.internal.domain.metamodel.XMLSerializerModel
import amf.shapes.internal.spec.ShapeParserContext
import org.yaml.model.{YMap, YNode}

object XMLSerializerParser {
  def parse(defaultName: String)(node: YNode)(implicit ctx: ShapeParserContext): XMLSerializer =
    XMLSerializerParser(defaultName, node).parse()
}

case class XMLSerializerParser(defaultName: String, node: YNode)(implicit ctx: ShapeParserContext) {
  val map: YMap = node.as[YMap]
  def parse(): XMLSerializer = {
    val serializer = XMLSerializer(node)
      .set(XMLSerializerModel.Attribute, value = false)
      .set(XMLSerializerModel.Wrapped, value = false)

    map.key(
      "attribute",
      entry => {
        val value = ScalarNode(entry.value)
        serializer.set(XMLSerializerModel.Attribute, value.boolean(), Annotations(entry) += ExplicitField())
      }
    )

    map.key("wrapped", entry => {
      val value = ScalarNode(entry.value)
      serializer.set(XMLSerializerModel.Wrapped, value.boolean(), Annotations(entry) += ExplicitField())
    })

    map.key("name", entry => {
      val value = ScalarNode(entry.value)
      serializer.set(XMLSerializerModel.Name, value.string(), Annotations(entry) += ExplicitField())
    })

    map.key("namespace", entry => {
      val value = ScalarNode(entry.value)
      serializer.set(XMLSerializerModel.Namespace, value.string(), Annotations(entry))
    })

    map.key("prefix", entry => {
      val value = ScalarNode(entry.value)
      serializer.set(XMLSerializerModel.Prefix, value.string(), Annotations(entry))
    })

    ctx.closedShape(serializer.id, map, "xmlSerialization")

    serializer
  }
}
