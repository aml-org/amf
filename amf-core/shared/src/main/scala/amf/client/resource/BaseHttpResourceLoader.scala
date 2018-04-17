package amf.client.resource

import amf.core.remote.HttpParts

abstract class BaseHttpResourceLoader extends ResourceLoader {

  override def accepts(resource: String): Boolean = resource match {
    case HttpParts(_, _, _) => true
    case _                  => false
  }
}
