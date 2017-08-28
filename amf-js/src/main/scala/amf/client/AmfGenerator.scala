package amf.client

import amf.remote.Amf
import amf.remote.Syntax.Json

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Amf]] generator.
  */
@JSExportTopLevel("AmfGenerator")
class AmfGenerator extends BaseGenerator(Amf, Json)
