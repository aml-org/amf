package amf.client.resource

import amf.core.remote.HttpParts

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
abstract class BaseHttpResourceLoader extends ResourceLoader {

  override def accepts(resource: String): Boolean = resource match {
    case HttpParts(_, _, _) => true
    case _                  => false
  }
}
