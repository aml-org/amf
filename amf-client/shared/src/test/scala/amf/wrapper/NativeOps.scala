package amf.wrapper

import amf.client.convert.CoreClientConverters._

import scala.concurrent.Future

trait NativeOps {

  implicit def toNativeFuture[T](client: ClientFuture[T]): NativeFuture[T]
  implicit def toNativeList[E](client: ClientList[E]): NativeList[E]

  implicit class SeqOps[E](seq: Seq[E]) {
    def toClient: ClientList[E] = asClientList(seq, new IdentityMatcher[E] {})
  }

  trait NativeFuture[T] {
    val native: ClientFuture[T]
    def asFuture: Future[T]
  }

  trait NativeList[E] {
    val native: ClientList[E]
    def asSeq: Seq[E]
  }
}
