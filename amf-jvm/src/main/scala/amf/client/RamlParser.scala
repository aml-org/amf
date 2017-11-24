package amf.client

import amf.framework.remote.Raml
import amf.framework.remote.Syntax.Yaml

/**
  * [[Raml]] parser.
  */
class RamlParser extends BaseParser(Raml, Yaml)
