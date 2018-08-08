package amf.core.remote.server

import amf.client.remote.Content
import amf.client.resource.BaseHttpResourceLoader
import amf.core.remote.{NetworkError, UnexpectedStatusCode}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.Dictionary
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("JsServerHttpResourceLoader")
@JSExportAll
case class JsServerHttpResourceLoader() extends BaseHttpResourceLoader {

  override def fetch(resource: String): js.Promise[Content] = {
    val promise: Promise[Content] = Promise()

    if (resource.startsWith("https:")) {
      try {
        val req = Https.get(
          resource,
          handleResponse(resource, promise)
        )
        req.on("error", handleError(promise))
      } catch {
        case e: Throwable =>
          promise.failure(NetworkError(e))
      }

    } else {
      try {
        val req = Http.get(
          resource,
          handleResponse(resource, promise)
        )
        req.on("error", handleError(promise))
      } catch {
        case e: Throwable =>
          promise.failure(NetworkError(e))
      }
    }

    promise.future.recover {
      case e: Throwable =>
        throw NetworkError(e)
    }.toJSPromise
  }

  private def handleError(promise: Promise[Content]): js.Function =
    (e: Any) => promise.failure(NetworkError(new Exception(e.toString)))

  private def handleResponse(resource: String, promise: Promise[Content]): js.Function1[js.Dynamic, Any] = {
    (response: js.Dynamic) =>
      {
        var str  = ""
        val code = response.statusCode.asInstanceOf[Int]

        if (code >= 300) promise.failure(UnexpectedStatusCode(resource, code))
        else {
          // CAREFUL!
          // this is required to avoid undefined behaviours
          val dataCb: js.Function1[Any, Any] = { (res: Any) =>
            str += res.toString
          }
          // Another chunk of data has been received, append it to `str`
          response.on("data", dataCb)

          val completedCb: js.Function = () => {
            val mediaType = try {
              Some(response.headers.asInstanceOf[Dictionary[String]]("content-type"))
            } catch {
              case e: Throwable => None
            }
            promise.success(new Content(str, resource, mediaType))
          }
          response.on("end", completedCb)
        }
      }

  }

}
