package amf.client.resource

import amf.client.convert.CoreClientConverters._
import amf.client.remote.Content
import amf.core.remote.File
import amf.core.remote.File.FILE_PROTOCOL

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait BaseFileResourceLoader extends ResourceLoader {
  override def fetch(resource: String): ClientFuture[Content] = fetchFile(resource.stripPrefix(FILE_PROTOCOL))

  def fetchFile(resource: String): ClientFuture[Content]

  override def accepts(resource: String): Boolean = resource match {
    case File(_) => true
    case _       => false
  }
}
