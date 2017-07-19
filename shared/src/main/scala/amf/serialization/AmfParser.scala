package amf.serialization

import amf.parser.{BaseAMFParser, YeastASTBuilder}
import amf.remote.{Amf, Vendor}

/** [[Amf]] parser */
class AmfParser(b: YeastASTBuilder) extends BaseAMFParser(b) {
  override def vendor(): Vendor = Amf
}
