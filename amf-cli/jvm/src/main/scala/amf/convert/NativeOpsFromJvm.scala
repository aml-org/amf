package amf.convert

import amf.cli.convert.NativeOps

import java.util
import java.util.concurrent.CompletableFuture

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._
import scala.concurrent.Future
import scala.language.implicitConversions

private[amf] trait NativeOpsFromJvm extends NativeOps {

  override implicit def toNativeOption[E](client: ClientOption[E]): NativeOption[E]    = new JvmNativeOption[E](client)
  override implicit def toNativeList[E](client: ClientList[E]): JvmNativeList[E]       = new JvmNativeList(client)
  override implicit def toNativeFuture[T](client: ClientFuture[T]): JvmNativeFuture[T] = new JvmNativeFuture(client)

  protected class JvmNativeOption[E](list: ClientOption[E]) extends NativeOption[E] {
    override val native: util.Optional[E] = list.asInstanceOf[util.Optional[E]]
    override def asOption: Option[E]      = native.asScala
  }

  protected class JvmNativeList[E](list: ClientList[E]) extends NativeList[E] {
    override val native: util.List[E] = list.asInstanceOf[util.List[E]]
    override def asSeq: Seq[E]        = native.asScala
  }

  protected class JvmNativeFuture[T](future: ClientFuture[T]) extends NativeFuture[T] {
    override val native: CompletableFuture[T] = future.asInstanceOf[CompletableFuture[T]]
    override def asFuture: Future[T]          = native.toScala
  }
}
