package amf.client

import amf.framework.remote.Raml
import amf.framework.remote.Syntax.Yaml

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Raml]] generator.
  */
@JSExportTopLevel("RamlGenerator")
class RamlGenerator extends BaseGenerator(Raml, Yaml)
