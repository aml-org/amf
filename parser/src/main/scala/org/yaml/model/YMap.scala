package org.yaml.model

/**
  * A Yaml Map
  */
class YMap(c: IndexedSeq[YamlPart]) extends YAggregate(c) {
  override def indentedString(n: Int): String = " " * n + "{\n" + super.indentedString(n) + " " * n + "}"
}

class YMapEntry private (val key: YNode, val value: YNode, children_ : IndexedSeq[YamlPart])
    extends YAggregate(children_) {
  override def indentedString(n: Int): String = " " * n + key + ": " + value
}
object YMapEntry {
  def apply(parts: IndexedSeq[YamlPart]): YMapEntry = {
    val kv = parts collect { case a: YNode => a }
    new YMapEntry(kv(0), kv(1), parts)
  }
}
