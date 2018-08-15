package amf.client.resolve

import amf.core.remote.Raml10

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("Raml10Resolver")
class Raml10Resolver extends Resolver(Raml10.name)
