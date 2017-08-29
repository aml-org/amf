package amf.client

import amf.remote.Oas
import amf.remote.Syntax.Json

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[amf.remote.Oas]] parser.
  */
@JSExportTopLevel("OasParser")
class OasParser extends BaseParser(Oas, Json)
