package amf.client.resolve

import amf.core.remote.Raml08

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("Raml08Resolver")
class Raml08Resolver extends Resolver(Raml08.name)
