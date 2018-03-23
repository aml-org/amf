package amf.wrapper

import java.util
import java.util.concurrent.CompletableFuture

import amf.client.convert.VocabulariesClientConverter._

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._
import scala.concurrent.Future

class JvmWrapperTests extends WrapperTests {

  override implicit def toNativeFuture[T](client: ClientFuture[T]): JvmNativeFuture[T] =
    new JvmNativeFuture(client)

  override implicit def toNativeList[E](client: ClientList[E]): JvmNativeList[E] =
    new JvmNativeList(client)

  protected class JvmNativeFuture[T](future: ClientFuture[T]) extends NativeFuture[T] {
    val native: CompletableFuture[T] = future.asInstanceOf[CompletableFuture[T]]
    def asFuture: Future[T]          = native.toScala
  }

  protected class JvmNativeList[E](list: ClientList[E]) extends NativeList[E] {
    val native: util.List[E]  = list.asInstanceOf[util.List[E]]
    def size: Int             = native.size()
    def head: E               = native.get(0)
    def has(elem: E): Boolean = native.contains(elem)
    def asSeq: Seq[E]         = native.asScala
  }
}
