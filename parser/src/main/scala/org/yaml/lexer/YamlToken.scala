package org.yaml.lexer

import org.mulesoft.lexer.Token

import scala.collection.mutable

/**
  * YamlToken
  */
sealed class YamlToken(name: String, val abbreviation: String) extends Token(name) {
  def this(name: String) = this(name, name.substring(0, 1))
  YamlToken.tokens.put(abbreviation, this)

  override def toString: String = name
}

object YamlToken {
  private final val tokens = new mutable.HashMap[String, YamlToken]()

  /** Separation line break. */
  final val LineBreak = new YamlToken("LineBreak", "b")

  /** Begins comment. */
  final val BeginComment = new YamlToken("BeginComment", "C")

  /** Ends comment. */
  final val EndComment = new YamlToken("EndComment", "c")

  /** Begins directive. */
  final val BeginDirective = new YamlToken("BeginDirective", "D")

  /** Ends directive */
  final val EndDirective = new YamlToken("EndDirective", "d")

  /** Character indicating structure. */
  final val Indicator = new YamlToken("Indicator", "I")

  /** Indentation spaces. */
  final val Indent = new YamlToken("Indent", "i")

  /** Document start marker (Directives End). */
  final val DirectivesEnd = new YamlToken("DirectivesEnd", "K")

  /** Line break folded to content space. */
  final val LineFold = new YamlToken("LineFold", "l")

  /** Begins complete node */
  final val BeginNode = new YamlToken("BeginNode", "N")

  /** Ends complete node */
  final val EndNode = new YamlToken("EndNode", "n")

  /** Begins document */
  final val BeginDocument = new YamlToken("BeginDocument", "O")

  /** Ends document */
  final val EndDocument = new YamlToken("EndDocument", "o")

  /** Begins sequence content */
  final val BeginSequence = new YamlToken("BeginSequence", "Q")

  /** Ends sequence content */
  final val EndSequence = new YamlToken("EndSequence", "q")

  /** Begins mapping content */
  final val BeginMapping = new YamlToken("BeginMapping", "M")

  /** Ends mapping content */
  final val EndMapping = new YamlToken("EndMapping", "m")

  /** Begins scalar content */
  final val BeginScalar = new YamlToken("BeginScalar", "S")

  /** Ends scalar content */
  final val EndScalar = new YamlToken("EndScalar", "s")

  /** Content text characters. */
  final val Text = new YamlToken("Text", "T")

  /** Non-content (meta) text characters. */
  final val MetaText = new YamlToken("MetaText", "t")

  /** BOM, contains TF8, TF16LE, TF32BE, etc. */
  final val Bom = new YamlToken("Bom", "U")

  /** Separation white space. */
  final val WhiteSpace = new YamlToken("WhiteSpace", "w")

  /** Ends Yaml Stream */
  final val EndStream = new YamlToken("EndStream", "")

  /** Begins YAML stream */
  final val BeginStream = new YamlToken("BeginStream", "")

  /** Parsing error at this point */
  final val Error = new YamlToken("Error", "!")

  def apply(abbreviation: String): YamlToken =
    tokens.apply(abbreviation)
}
