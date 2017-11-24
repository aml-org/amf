package amf.client

import amf.framework.remote.Amf
import amf.framework.remote.Syntax.Json

/**
  * [[Amf]] parser.
  */
class AmfParser extends BaseParser(Amf, Json)
