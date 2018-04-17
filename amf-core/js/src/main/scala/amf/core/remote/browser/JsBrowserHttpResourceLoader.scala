package amf.core.remote.browser

import amf.client.remote.Content
import amf.client.resource.JsHttpResourceLoader
import amf.core.remote.FileNotFound
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

case class JsBrowserHttpResourceLoader() extends JsHttpResourceLoader {

  override def fetch(resource: String): js.Promise[Content] = {
    Ajax
      .get(resource)
      .flatMap(xhr =>
        xhr.status match {
          case 200 => Future.successful(new Content(xhr.responseText, resource))
          case s   => Future.failed(FileNotFound(new Exception(s"Unhandled status code $s with ${xhr.statusText}")))
      })
      .toJSPromise
  }
}
