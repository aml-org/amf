package amf.core.interop

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
  def get(url: String, callback: js.Function1[ServerResponse, Any]): Unit = js.native
}

@js.native
@JSImport("http", JSImport.Namespace, "http")
object Http extends Http

@js.native
trait ServerResponse extends js.Object {

  /**
    * Adds the listener function to the end of the listeners array for the event named eventName.
    * No checks are made to see if the listener has already been added. Multiple calls passing
    * the same combination of eventName and listener will result in the listener being added,
    * and called, multiple times.
    * <p/>Returns a reference to the EventEmitter so calls can be chained.
    * @example emitter.on(eventName, listener)
    */
  def on(eventName: String, listener: js.Function): this.type = js.native
}
