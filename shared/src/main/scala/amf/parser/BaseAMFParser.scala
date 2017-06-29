package amf.parser

import amf.common.AMFToken._
import amf.remote.Vendor

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

  private def parseSequence(): Unit =
    parseList(SequenceToken, StartSequence, Comma, EndSequence, () => {
      parseValue()
      true
    })

  private def parseMapping(): Unit = parseList(MapToken, StartMap, Comma, EndMap, parseEntry)

  protected def parseEntry(): Boolean = entry(parseValue)

  protected def entry(value: () => Unit = parseValue): Boolean = {
    beginTree()
    matchOrError(StringToken)
    if (currentEq(Colon)) discard()
    value()
    endTree(Entry)
    true
  }

  protected def parseValue(): Unit = {
    current match {
      case StringToken | IntToken | FloatToken | True | False | Null => consume()
      case _                                                         => parse()
    }
  }

  def vendor(): Vendor
}
