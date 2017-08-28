package amf.client

import amf.remote.Oas
import amf.remote.Syntax.Json

/**
  * [[Oas]] parser.
  */
class OasParser extends BaseParser(Oas, Json)
