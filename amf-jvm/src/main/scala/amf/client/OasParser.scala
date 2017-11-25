package amf.client

import amf.core.remote.Oas
import amf.core.remote.Syntax.Json

/**
  * [[Oas]] parser.
  */
class OasParser extends BaseParser(Oas, Json)
