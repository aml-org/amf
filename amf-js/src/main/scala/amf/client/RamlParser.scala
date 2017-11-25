package amf.client

import amf.core.remote.Raml
import amf.core.remote.Syntax.Yaml

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Raml]] parser.
  */
@JSExportTopLevel("RamlParser")
class RamlParser extends BaseParser(Raml, Yaml)
