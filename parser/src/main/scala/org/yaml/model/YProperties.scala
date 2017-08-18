package org.yaml.model

import org.yaml.lexer.YeastToken

/**
  * Yaml Properties Node
  */
class YProperties private (val anchor: Option[YAnchor], val tag: Option[YTag], c: IndexedSeq[YamlPart])
    extends YAggregate(c) {
  override def indentedString(n: Int): String = List(anchor.map(" &" + _), tag.map(" !" + _)).flatten.mkString
}

object YProperties {

  /** Empty Properties */
  val Empty = new YProperties(None, None, IndexedSeq.empty)

  /** Constructor */
  def apply(parts: IndexedSeq[YamlPart]): YProperties = {
    if (parts.isEmpty) return Empty

    var alias: Option[YAnchor] = None
    var tag: Option[YTag]      = None

    for (p <- parts) p match {
      case a: YAnchor => alias = Some(a)
      case t: YTag    => tag = Some(t)
      case _          =>
    }
    new YProperties(alias, tag, parts)
  }

}

class YAnchor(val name: String, ts: IndexedSeq[YeastToken]) extends YTokens(ts) {
  override def toString: String = name
}

class YTag(val tag: String, ts: IndexedSeq[YeastToken]) extends YTokens(ts) {
  override def toString: String = tag
}
