package amf.client

import amf.framework.remote.Amf
import amf.framework.remote.Syntax.Json

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Amf]] parser.
  */
@JSExportTopLevel("AmfParser")
class AmfParser extends BaseParser(Amf, Json)
