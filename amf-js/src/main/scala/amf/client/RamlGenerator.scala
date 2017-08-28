package amf.client

import amf.remote.Raml
import amf.remote.Syntax.Yaml

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Raml]] generator.
  */
@JSExportTopLevel("RamlGenerator")
class RamlGenerator extends BaseGenerator(Raml, Yaml)
