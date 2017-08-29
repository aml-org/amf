package amf.client

import amf.remote.Raml
import amf.remote.Syntax.Yaml

/**
  * [[amf.remote.Raml]] generator.
  */
class RamlGenerator extends BaseGenerator(Raml, Yaml)
