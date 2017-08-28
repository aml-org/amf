package amf.client

import amf.remote.Amf
import amf.remote.Syntax.Json

/**
  * [[Amf]] parser.
  */
class AmfParser extends BaseParser(Amf, Json)
