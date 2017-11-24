package amf.client

import amf.framework.remote.Raml
import amf.framework.remote.Syntax.Yaml

import scala.scalajs.js.annotation.JSExportTopLevel

/**
  * [[Raml]] parser.
  */
@JSExportTopLevel("RamlParser")
class RamlParser extends BaseParser(Raml, Yaml)
