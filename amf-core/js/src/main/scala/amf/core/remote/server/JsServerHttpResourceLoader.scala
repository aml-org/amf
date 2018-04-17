package amf.core.remote.server

import amf.client.remote.Content
import amf.client.resource.JsHttpResourceLoader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

case class JsServerHttpResourceLoader() extends JsHttpResourceLoader {

  override def fetch(resource: String): js.Promise[Content] = {
    val promise: Promise[Content] = Promise()

    if (resource.startsWith("https:")) {
      Https.get(
        resource,
        (response: js.Dynamic) => {
          var str = ""

          // CAREFUL!
          // this is required to avoid undefined behaviours
          val dataCb: js.Function1[Any, Any] = { (res: Any) =>
            str += res.toString
          }
          // Another chunk of data has been received, append it to `str`
          response.on("data", dataCb)

          val completedCb: js.Function = () => {
            val mediaType = try {
              Some(response.headers.asInstanceOf[js.Dictionary[String]]("content-type"))
            } catch {
              case e: Throwable => None
            }
            promise.success(new Content(str, resource, mediaType))
          }
          response.on("end", completedCb)
        }
      )

    } else {
      Http.get(
        resource,
        (response: js.Dynamic) => {
          var str = ""

          val dataCb: js.Function1[Any, Any] = { (res: Any) =>
            str += res.toString
          }
          // Another chunk of data has been received, append it to `str`
          response.on("data", dataCb)

          val completedCb: js.Function = () => {
            val mediaType = try {
              Some(response.headers.asInstanceOf[js.Dictionary[String]]("content-type"))
            } catch {
              case e: Throwable => None
            }
            promise.success(new Content(str, resource, mediaType))
          }
          response.on("end", completedCb)
        }
      )
    }

    promise.future.toJSPromise
  }
}
