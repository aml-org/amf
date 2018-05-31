package amf.core.remote.server

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  *
  */
@js.native
trait Http extends js.Object {

  /**
    * Since most requests are GET requests without bodies, Node.js provides this convenience method. The only difference
    * between this method and http.request() is that it sets the method to GET and calls req.end() automatically.
    * @example http.get('https://encrypted.google.com/', (res) => { ... })
    */
  def get(url: String, callback: js.Function1[js.Dynamic, Any]): js.Dynamic = js.native
}

@js.native
@JSImport("http", JSImport.Namespace, "http")
object Http extends Http



/**
  *
  */
@js.native
trait Https extends js.Object {

  /**
    * Since most requests are GET requests without bodies, Node.js provides this convenience method. The only difference
    * between this method and http.request() is that it sets the method to GET and calls req.end() automatically.
    * @example http.get('https://encrypted.google.com/', (res) => { ... })
    */
  def get(url: String, callback: js.Function1[js.Dynamic, Any]): js.Dynamic = js.native
}

@js.native
@JSImport("https", JSImport.Namespace, "https")
object Https extends Https