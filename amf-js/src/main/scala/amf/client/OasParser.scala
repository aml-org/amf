package amf.client

import amf.framework.remote.Oas
import amf.framework.remote.Syntax.Json

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Oas]] parser.
  */
@JSExportTopLevel("OasParser")
class OasParser extends BaseParser(Oas, Json)
