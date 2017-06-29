package amf.oas

import amf.common.AMFToken._
import amf.parser.{BaseAMFParser, YeastASTBuilder}
import amf.remote.{Oas, Vendor}

class OASParser(b: YeastASTBuilder) extends BaseAMFParser(b) {

  private def link(): Boolean = {
    discardOrError(StringToken)
    if (currentEq(Colon)) discard()
    matchOrError(StringToken)
    discardOrError(EndMap)
    endTree(Link)
    false
  }

  /** Parse map entry input. */
  override protected def parseEntry(): Boolean = {
    currentText match {
      case "\"$ref\"" | "$ref" => link()
      case _                   => entry()
    }
  }

  override def vendor(): Vendor = Oas
}
