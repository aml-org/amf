package amf.parser

import amf.common.AMFToken._

abstract class BaseAMFParser(b: YeastASTBuilder) extends BaseParser(b) {

  /** Parse current input. */
  override def parse(): Unit = {
    while (currentEq(StartMap, StartSequence, Comment)) {
      current match {
        case StartMap      => parseMapping()
        case StartSequence => parseSequence()
        case _             => discard()
      }
    }
  }

  private def parseSequence(): Unit = parseList(SequenceToken, StartSequence, Comma, EndSequence, parseValue)

  private def parseMapping(): Unit = parseList(MapToken, StartMap, Comma, EndMap, parseEntry)

  protected def parseEntry(): Boolean = entry()

  protected def entry(): Boolean = {
    beginTree()
    matchOrError(StringToken)
    if (currentEq(Colon)) discard()
    parseValue()
    endTree(Entry)
    true
  }

  protected def parseValue(): Boolean = {
    current match {
      case StringToken | IntToken | FloatToken | True | False | Null => consume()
      case _                                                         => parse()
    }
    true
  }
}
