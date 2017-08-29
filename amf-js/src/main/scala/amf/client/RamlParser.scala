package amf.client

import amf.remote.Raml
import amf.remote.Syntax.Yaml

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[amf.remote.Raml]] parser.
  */
@JSExportTopLevel("RamlParser")
class RamlParser extends BaseParser(Raml, Yaml)
