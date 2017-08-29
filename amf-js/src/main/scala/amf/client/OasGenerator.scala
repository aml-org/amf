package amf.client

import amf.remote.Oas
import amf.remote.Syntax.Json

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[amf.remote.Oas]] generator.
  */
@JSExportTopLevel("OasGenerator")
class OasGenerator extends BaseGenerator(Oas, Json)
