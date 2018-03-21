package amf.client.convert

import java.util
import java.util.concurrent.CompletableFuture

import amf.client.handler.{FileHandler, Handler}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.compat.java8.FutureConverters
import scala.concurrent.Future

trait CoreBaseClientConverter extends CoreBaseConverter {

  override type ClientList[E] = util.List[E]
  override type ClientMap[V]  = util.Map[String, V]

  override type ClientFuture[T] = CompletableFuture[T]

  override type ClientResultHandler[T] = Handler[T]
  override type ClientFileHandler      = FileHandler

  override private[convert] def asClientList[A, B](from: Seq[A], matcher: InternalClientMatcher[A, B]): util.List[B] =
    from.map(matcher.asClient).asJava

  override private[convert] def asClientMap[Internal, Client](from: mutable.Map[String, Internal],
                                                              matcher: InternalClientMatcher[Internal, Client]) = {
    from.map { case (k, v) => k -> matcher.asClient(v) }.asJava
  }

  override private[convert] def asInternalSeq[Client, Internal](from: util.List[Client],
                                                                matcher: ClientInternalMatcher[Client, Internal]) =
    from.asScala.map(matcher.asInternal)

  override private[convert] def asClientFuture[T](from: Future[T]): ClientFuture[T] =
    FutureConverters.toJava(from).toCompletableFuture
}
