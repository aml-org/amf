package amf.client

import amf.remote.Raml
import amf.remote.Syntax.Yaml

/**
  * [[Raml]] generator.
  */
class RamlGenerator extends BaseGenerator(Raml, Yaml)
