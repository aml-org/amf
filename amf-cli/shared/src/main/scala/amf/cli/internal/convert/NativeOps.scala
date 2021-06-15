package amf.cli.internal.convert

import amf.core.internal.convert.CoreClientConverters.{ClientFuture, ClientList, ClientOption}
import scala.language.implicitConversions
import scala.concurrent.Future

trait NativeOps {

  implicit def toNativeOption[E](client: ClientOption[E]): NativeOption[E]
  implicit def toNativeList[E](client: ClientList[E]): NativeList[E]
  implicit def toNativeFuture[T](client: ClientFuture[T]): NativeFuture[T]

  trait NativeOption[E] {
    val native: ClientOption[E]
    def asOption: Option[E]
  }

  trait NativeList[E] {
    val native: ClientList[E]
    def asSeq: Seq[E]
  }

  trait NativeFuture[T] {
    val native: ClientFuture[T]
    def asFuture: Future[T]
  }
}
