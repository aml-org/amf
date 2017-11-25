package amf.client

import amf.core.remote.Raml
import amf.core.remote.Syntax.Yaml

/**
  * [[Raml]] parser.
  */
class RamlParser extends BaseParser(Raml, Yaml)
