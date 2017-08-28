package amf.client

import amf.remote.Amf
import amf.remote.Syntax.Json

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Amf]] parser.
  */
@JSExportTopLevel("AmfParser")
class AmfParser extends BaseParser(Amf, Json)
