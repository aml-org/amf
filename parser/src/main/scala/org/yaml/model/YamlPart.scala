package org.yaml.model

import org.yaml.lexer.YeastToken
import org.mulesoft.common.core._

/**
  * A Yaml Part
  */
trait YamlPart {
  def children: IndexedSeq[YamlPart] = IndexedSeq.empty
  def indentedString(n: Int): String = " " * n + toString
}

trait YamlValue extends YamlPart

/** A Set of Yaml Tokens */
abstract class YTokens(val tokens: IndexedSeq[YeastToken]) extends YamlPart {
  override def toString: String = tokens.mkString(", ")
}

/** Non Content Yaml Tokens */
class YNonContent(ts: IndexedSeq[YeastToken]) extends YTokens(ts)

class YScalar(val text: String, ts: IndexedSeq[YeastToken]) extends YTokens(ts) with YamlValue {
  override def toString: String = '"' + text.encode + '"'
}

abstract class YAggregate(override val children: IndexedSeq[YamlPart]) extends YamlValue {
  override def indentedString(n: Int): String =
    children.filterNot(_.isInstanceOf[YNonContent]).map(_.indentedString(n + 2) + "\n").mkString
  override def toString: String = indentedString(0)
}

class YDocument(c: IndexedSeq[YamlPart]) extends YAggregate(c) {
  override def toString: String = "Document:\n" + super.toString
}


class YSequence(c: IndexedSeq[YamlPart]) extends YAggregate(c) {
  override def indentedString(n: Int): String = " " * n + "[\n" + super.indentedString(n) + " " * n + "]"
}


class YAlias(val name: String, ts: IndexedSeq[YeastToken]) extends YTokens(ts) {
    override def toString: String = name
}

