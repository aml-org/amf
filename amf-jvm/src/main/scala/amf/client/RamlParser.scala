package amf.client

import amf.remote.Raml
import amf.remote.Syntax.Yaml

/**
  * [[amf.remote.Raml]] parser.
  */
class RamlParser extends BaseParser(Raml, Yaml)
