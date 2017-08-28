package amf.client

import amf.remote.{Amf, Oas}
import amf.remote.Syntax.Json

/**
  * [[Amf]] generator.
  */
class AmfGenerator extends BaseGenerator(Amf, Json)
