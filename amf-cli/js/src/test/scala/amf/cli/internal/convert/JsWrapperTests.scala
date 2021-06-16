package amf.cli.internal.convert

import amf.client.convert.VocabulariesClientConverter.{ClientFuture, ClientList, ClientOption}

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.scalajs.js

class JsWrapperTests extends WrapperTests with NativeOpsFromJs {
//  override def getAbsolutePath(path: String): String = {
//    // temp: absolute path should be implemented at common.
//    // We need to figured out how to get absolute path from js server platform.
//    if (path.startsWith("/")) "file:/" + path
//    else "file://" + path
//  }

}

private[amf] trait NativeOpsFromJs extends NativeOps {

  override implicit def toNativeOption[E](client: ClientOption[E]): NativeOption[E]   = new JsNativeOption[E](client)
  override implicit def toNativeList[E](client: ClientList[E]): JsNativeList[E]       = new JsNativeList(client)
  override implicit def toNativeFuture[T](client: ClientFuture[T]): JsNativeFuture[T] = new JsNativeFuture(client)

  protected class JsNativeOption[E](list: ClientOption[E]) extends NativeOption[E] {
    override val native: js.UndefOr[E] = list.asInstanceOf[js.UndefOr[E]]
    override def asOption: Option[E]   = native.toOption
  }

  protected class JsNativeList[E](list: ClientList[E]) extends NativeList[E] {
    override val native: js.Array[E] = list.asInstanceOf[js.Array[E]]
    override def asSeq: Seq[E]       = native.toSeq
  }

  protected class JsNativeFuture[T](future: ClientFuture[T]) extends NativeFuture[T] {
    override val native: js.Promise[T] = future.asInstanceOf[js.Promise[T]]
    override def asFuture: Future[T]   = native.toFuture
  }
}
