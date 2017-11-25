package amf.client

import amf.core.remote.Amf
import amf.core.remote.Syntax.Json

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Amf]] parser.
  */
@JSExportTopLevel("AmfParser")
class AmfParser extends BaseParser(Amf, Json)
