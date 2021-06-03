package amf.cli.convert

import amf.client.convert.CoreClientConverters._

import scala.concurrent.Future
import scala.language.implicitConversions

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
