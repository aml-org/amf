package amf.core.remote.browser

import amf.client.remote.Content
import amf.client.resource.BaseHttpResourceLoader
import amf.core.remote.{NetworkError, UnexpectedStatusCode}
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("JsBrowserHttpResourceLoader")
@JSExportAll
case class JsBrowserHttpResourceLoader() extends BaseHttpResourceLoader {

  override def fetch(resource: String): js.Promise[Content] = {
    Ajax
      .get(resource)
      .flatMap(xhr =>
        xhr.status match {
          case 200 => Future.successful(new Content(xhr.responseText, resource))
          case s   => Future.failed(UnexpectedStatusCode(resource, s))
      })
      .recover {
        case e: Throwable => throw NetworkError(e)
      }
      .toJSPromise
  }
}
