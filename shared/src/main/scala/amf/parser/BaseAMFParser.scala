package amf.parser

import amf.common.AMFToken
import amf.common.AMFToken._
import amf.remote.Vendor

/** Parse tokens into ast tree. */
abstract class BaseAMFParser(b: YeastASTBuilder) extends BaseParser(b) {

  /** Parse current input. */
  override def parse(): Unit = {
    while (currentEq(StartMap, StartSequence, Comment)) {
      current match {
        case StartMap      => parseMapping()
        case StartSequence => parseSequence()
        case Comment       => parseComment()
      }
    }
  }

  private def parseSequence(): Unit =
    parseList(SequenceToken, StartSequence, Comma, EndSequence, () => {
      parseValue()
      true
    })

  private def parseMapping(): Unit = parseList(MapToken, StartMap, Comma, EndMap, parseEntry)

  protected def parseEntry(): Boolean = entry()

  protected def entry(value: () => Unit = parseValue, token: AMFToken = Entry): Boolean = {
    beginTree()
    matchOrError(StringToken)
    discard(Colon)
    value()
    endTree(token)
    true
  }

  protected def parseValue(): Unit = {
    current match {
      case StringToken | IntToken | FloatToken | BooleanToken | Null => consume()
      case _                                                         => parse()
    }
  }

  def vendor(): Vendor

  private def parseComment(): Unit = matchOrError(Comment)
}
