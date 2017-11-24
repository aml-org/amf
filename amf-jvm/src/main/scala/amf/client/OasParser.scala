package amf.client

import amf.framework.remote.Oas
import amf.framework.remote.Syntax.Json

/**
  * [[Oas]] parser.
  */
class OasParser extends BaseParser(Oas, Json)
