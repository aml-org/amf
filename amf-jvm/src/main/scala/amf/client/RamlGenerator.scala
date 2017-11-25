package amf.client

import amf.core.remote.Raml
import amf.core.remote.Syntax.Yaml

/**
  * [[Raml]] generator.
  */
class RamlGenerator extends BaseGenerator(Raml, Yaml)
