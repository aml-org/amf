package amf.client

import amf.remote.Oas
import amf.remote.Syntax.Json

/**
  * [[amf.remote.Oas]] parser.
  */
class OasParser extends BaseParser(Oas, Json)
