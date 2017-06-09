package amf.yaml

import amf.parser.{BaseASTBuilder, BaseParser, Parser}
import amf.yaml.YamlToken._

/**
  * Created by pedro.colunga on 5/23/17.
  */
class YamlParser(b: BaseASTBuilder[YamlToken, YamlAST]) extends BaseParser(b) {

    /** Parse current input. */
    override def parse(): Unit = {
        while (currentEq(StartMap, StartSequence, Comment)) {
            current match {
                case StartMap => parseMap()
                case StartSequence => parseSequence()
                case _ => discard()
            }
        }
    }

    def parseMap(): Unit = parseList(MapToken, StartMap, Comma, EndMap, new MapEntryParser)

    def parseSequence(): Unit = parseList(SequenceToken, StartSequence, Comma, EndSequence, new ArrayElementParser)

    def parseLink(): Unit = {
        beginTree()
        discard()
        matchOrError(StringToken)
        endTree(Link)
    }

class MapEntryParser extends Parser {
        /** Parse map entry input. */
        override def parse(): Unit = {
            beginTree()
            matchOrError(StringToken)
            parseYamlValue()
            endTree(Entry)
        }
    }

    private def parseYamlValue() = {
        current match {
            case StringToken => consume()
            case Link => parseLink()
            case _ => parse()
        }
    }

    class ArrayElementParser extends Parser {
        /** Parse array element input. */
        override def parse(): Unit = parseYamlValue()
    }
}
