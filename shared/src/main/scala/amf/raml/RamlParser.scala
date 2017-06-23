package amf.raml

import amf.common.AMFToken._
import amf.parser.{BaseAMFParser, YeastASTBuilder}

class RamlParser(b: YeastASTBuilder) extends BaseAMFParser(b) {

  private def link(): Boolean = {
    beginTree()
    discard()
    consume()
    endTree(Link)
    true
  }

  override protected def parseValue(): Unit = current match {
    case Tag if currentText equals "!include" => link()
    case _                                    => super.parseValue()
  }
}
