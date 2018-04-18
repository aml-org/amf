package amf.client.convert

import java.util
import java.util.Optional
import java.util.concurrent.CompletableFuture

import amf.client.handler.{FileHandler, Handler}
import amf.client.resource.{ResourceLoader => ClientResourceLoader}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.compat.java8.FutureConverters
import scala.compat.java8.OptionConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait CoreBaseClientConverter extends CoreBaseConverter {

  override type ClientOption[E] = util.Optional[E]
  override type ClientList[E]   = util.List[E]
  override type ClientMap[V]    = util.Map[String, V]

  override type ClientFuture[T] = CompletableFuture[T]

  override type ClientLoader = ClientResourceLoader

  override type ClientResultHandler[T] = Handler[T]
  override type ClientFileHandler      = FileHandler

  override protected def asClientOption[Internal, Client](
      from: Option[Internal],
      matcher: InternalClientMatcher[Internal, Client]): Optional[Client] =
    from.map(matcher.asClient).asJava

  override private[convert] def asClientList[A, B](from: Seq[A], matcher: InternalClientMatcher[A, B]): util.List[B] =
    from.map(matcher.asClient).asJava

  override protected def asClientMap[Internal, Client](
      from: mutable.Map[String, Internal],
      matcher: InternalClientMatcher[Internal, Client]): util.Map[String, Client] = {
    from.map { case (k, v) => k -> matcher.asClient(v) }.asJava
  }

  override protected def asInternalSeq[Client, Internal](
      from: util.List[Client],
      matcher: ClientInternalMatcher[Client, Internal]): mutable.Buffer[Internal] =
    from.asScala.map(matcher.asInternal)

  override protected def asClientFuture[T](from: Future[T]): ClientFuture[T] =
    FutureConverters.toJava(from).toCompletableFuture

  override protected def asInternalFuture[Client, Internal](
      from: CompletableFuture[Client],
      matcher: ClientInternalMatcher[Client, Internal]): Future[Internal] =
    FutureConverters.toScala(from).map(matcher.asInternal)

  override protected def toScalaOption[E](from: Optional[E]): Option[E] = from.asScala

  override protected def toClientOption[E](from: Option[E]): ClientOption[E] = from.asJava
}
