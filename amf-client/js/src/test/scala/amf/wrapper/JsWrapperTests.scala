package amf.wrapper

import amf.client.convert.VocabulariesClientConverter.{ClientFuture, ClientList}

import scala.concurrent.Future
import scala.scalajs.js

class JsWrapperTests extends WrapperTests {

  override implicit def toNativeFuture[T](client: ClientFuture[T]): JsNativeFuture[T] =
    new JsNativeFuture(client)

  override implicit def toNativeList[E](client: ClientList[E]): JsNativeList[E] =
    new JsNativeList(client)

  protected class JsNativeFuture[T](future: ClientFuture[T]) extends NativeFuture[T] {
    val native: js.Promise[T] = future.asInstanceOf[js.Promise[T]]
    def asFuture: Future[T]   = native.toFuture
  }

  protected class JsNativeList[E](list: ClientList[E]) extends NativeList[E] {
    val native: js.Array[E] = list.asInstanceOf[js.Array[E]]
    def asSeq: Seq[E]       = native.toSeq
  }
}
