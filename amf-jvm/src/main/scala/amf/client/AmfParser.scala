package amf.client

import amf.remote.Amf
import amf.remote.Syntax.Json

/**
  * [[amf.remote.Amf]] parser.
  */
class AmfParser extends BaseParser(Amf, Json)
