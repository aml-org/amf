package amf.client.resource

import amf.core.remote.FileLoaderException

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
@JSExportTopLevel("ResourceNotFound")
class ResourceNotFound(val msj: String) extends FileLoaderException(msj, new Throwable(msj))
