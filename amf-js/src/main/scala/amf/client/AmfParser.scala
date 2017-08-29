package amf.client

import amf.remote.Amf
import amf.remote.Syntax.Json

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[amf.remote.Amf]] parser.
  */
@JSExportTopLevel("AmfParser")
class AmfParser extends BaseParser(Amf, Json)
