package org.yaml.model

import scala.collection.mutable

/**
  * A Yaml Node
  */
class YNode private (val value: YamlValue, val properties: YProperties, c: IndexedSeq[YamlPart]) extends YAggregate(c) {
  assert(value!=null)
  override def indentedString(n: Int): String = value.indentedString(n) + properties
}

object YNode {
    /** Constructor */
    def apply(parts:IndexedSeq[YamlPart], aliases: mutable.Map[String, YNode]):YNode = {
        var properties = YProperties.Empty
        var value: YamlValue                = null

        for (p <- parts) p match {
            case ps: YProperties => properties = ps
            case v: YamlValue    => value = v
            case a: YAlias      =>
                val target = aliases(a.name)
                value = target.value
                properties = target.properties
            case _               =>
        }
        val node = new YNode(value, properties, parts)
        for (n <- node.properties.anchor) aliases += n.name -> node
        node
    }
}