package amf.oas

import amf.common.AMFToken._
import amf.parser.{BaseAMFParser, YeastASTBuilder}
import amf.remote.{Oas, Vendor}

class OASParser(b: YeastASTBuilder) extends BaseAMFParser(b) {

  private def link(): Boolean = {
    discardOrError(StringToken)
    discard(Colon)
    matchOrError(StringToken)
    discardOrError(EndMap)
    endTree(Link)
    false
  }

  private def extensions(): Boolean = {
    currentText match {
      case "x-uses" | "\"x-uses\"" => entry(() => libraries)
      case _                       => entry(() => extension)
    }
  }

  private def extension = {
    beginTree()
    matchOrError(StringToken)
    discard(Colon)
    parseValue()
    endTree(Extension)
    true
  }

  private def library(): Unit = {
    beginTree()
    consume()
    endTree(Library)
  }

  private def libraries = parseList(MapToken, StartMap, Comma, EndMap, () => entry(library))

  private def modules = {
    parseList(SequenceToken, StartSequence, Comma, EndSequence, () => {
      beginTree()
      matchOrError(StringToken)
      endTree(Library)
      true
    })
  }

  /** Parse map entry input. */
  override protected def parseEntry(): Boolean = {
    currentText match {
      case "\"$ref\"" | "$ref"                                   => link()
      case ext if ext.startsWith("x-") || ext.startsWith("\"x-") => extensions()
      case _                                                     => entry()
    }
  }

  override def vendor(): Vendor = Oas
}
