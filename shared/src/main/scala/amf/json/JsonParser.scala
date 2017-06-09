package amf.json

import amf.json.JsonToken._
import amf.parser.{BaseASTBuilder, BaseParser, Parser}

/**
  * Created by pedro.colunga on 5/23/17.
  */
class JsonParser(b: BaseASTBuilder[JsonToken, JsonAST]) extends BaseParser(b) {

  /** Parse current input. */
  override def parse(): Unit = {
    while (currentEq(StartMap, StartSequence)) {
      current match {
        case StartMap => parseMap()
        case StartSequence => parseSequence()
      }
    }
  }

  def parseMap(): Unit = {
    if (lookAhead(1) eq Link) {
      parseLink()
    } else {
      parseList(MapToken, StartMap, Comma, EndMap, new MapEntryParser)
    }
  }

  def parseSequence(): Unit =
    parseList(SequenceToken,
              StartSequence,
              Comma,
              EndSequence,
              new ArrayElementParser)

  def parseLink(): Unit = {
    beginTree()
    discardOrError(StartMap)
    discardOrError(Link)
    discardOrError(Colon)
    matchOrError(StringToken)
    discardOrError(EndMap)
    endTree(Link)
  }

  class MapEntryParser extends Parser {

    /** Parse map entry input. */
    override def parse(): Unit = {
      if (currentEq(Link)) {
        parseLink()
      } else {
        beginTree()
        matchOrError(StringToken)
        discardOrError(Colon)
        parseJsonValue()
        endTree(Entry)
      }
    }

  }

  private def parseJsonValue() = {
    current match {
      case StringToken | Number | True | False | Null => consume()
      case _ => parse()
    }
  }

  class ArrayElementParser extends Parser {

    /** Parse array element input. */
    override def parse(): Unit = parseJsonValue()
  }

}
