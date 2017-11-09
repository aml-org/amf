package amf.spec.declaration

import amf.domain.Annotation.ExplicitField
import amf.domain.Annotations
import amf.metadata.shape.{PropertyDependenciesModel, XMLSerializerModel}
import amf.model.{AmfArray, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape.{PropertyDependencies, PropertyShape, XMLSerializer}
import amf.spec.ParserContext
import amf.spec.common.{ArrayNode, ValueNode}
import org.yaml.model.{YMap, YMapEntry}

import scala.collection.mutable

/**
  *
  */
case class ShapeDependenciesParser(map: YMap, properties: mutable.ListMap[String, PropertyShape]) {
  def parse(): Seq[PropertyDependencies] = {
    map.entries.flatMap(entry => NodeDependencyParser(entry, properties).parse())
  }
}

case class NodeDependencyParser(entry: YMapEntry, properties: mutable.ListMap[String, PropertyShape]) {
  def parse(): Option[PropertyDependencies] = {

    properties
      .get(entry.key.value.toScalar.text)
      .map(p => {
        PropertyDependencies(entry)
          .set(PropertyDependenciesModel.PropertySource, AmfScalar(p.id), Annotations(entry.key))
          .set(PropertyDependenciesModel.PropertyTarget, AmfArray(targets()), Annotations(entry.value))
      })
  }

  private def targets(): Seq[AmfScalar] = {
    ArrayNode(entry.value.value.toSequence)
      .strings()
      .scalars
      .flatMap(v => properties.get(v.value.toString).map(p => AmfScalar(p.id, v.annotations)))
  }
}

case class XMLSerializerParser(defaultName: String, map: YMap)(implicit ctx: ParserContext) {
  def parse(): XMLSerializer = {
    val serializer = XMLSerializer(map)
      .set(XMLSerializerModel.Attribute, value = false)
      .set(XMLSerializerModel.Wrapped, value = false)
      .set(XMLSerializerModel.Name, defaultName)

    map.key(
      "attribute",
      entry => {
        val value = ValueNode(entry.value)
        serializer.set(XMLSerializerModel.Attribute, value.boolean(), Annotations(entry) += ExplicitField())
      }
    )

    map.key("wrapped", entry => {
      val value = ValueNode(entry.value)
      serializer.set(XMLSerializerModel.Wrapped, value.boolean(), Annotations(entry) += ExplicitField())
    })

    map.key("name", entry => {
      val value = ValueNode(entry.value)
      serializer.set(XMLSerializerModel.Name, value.string(), Annotations(entry) += ExplicitField())
    })

    map.key("namespace", entry => {
      val value = ValueNode(entry.value)
      serializer.set(XMLSerializerModel.Namespace, value.string(), Annotations(entry))
    })

    map.key("prefix", entry => {
      val value = ValueNode(entry.value)
      serializer.set(XMLSerializerModel.Prefix, value.string(), Annotations(entry))
    })

    ctx.closedShape(serializer.id, map, "xmlSerialization")

    serializer
  }
}
