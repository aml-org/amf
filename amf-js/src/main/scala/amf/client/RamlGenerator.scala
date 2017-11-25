package amf.client

import amf.core.remote.Raml
import amf.core.remote.Syntax.Yaml

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Raml]] generator.
  */
@JSExportTopLevel("RamlGenerator")
class RamlGenerator extends BaseGenerator(Raml, Yaml)
