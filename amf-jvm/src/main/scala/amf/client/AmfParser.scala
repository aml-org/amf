package amf.client

import amf.core.remote.Amf
import amf.core.remote.Syntax.Json

/**
  * [[Amf]] parser.
  */
class AmfParser extends BaseParser(Amf, Json)
