package amf.client

import amf.core.remote.Oas
import amf.core.remote.Syntax.Json

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Oas]] generator.
  */
@JSExportTopLevel("OasGenerator")
class OasGenerator extends BaseGenerator(Oas, Json)
