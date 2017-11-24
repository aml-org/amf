package amf.client

import amf.framework.remote.Oas
import amf.framework.remote.Syntax.Json

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Oas]] generator.
  */
@JSExportTopLevel("OasGenerator")
class OasGenerator extends BaseGenerator(Oas, Json)
