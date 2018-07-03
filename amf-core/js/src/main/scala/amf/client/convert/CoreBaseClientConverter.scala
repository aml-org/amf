package amf.client.convert

import amf.client.resource.{ClientResourceLoader, ResourceLoader}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.{Dictionary, Promise, UndefOr}

trait CoreBaseClientConverter extends CoreBaseConverter {

  override type ClientOption[E] = js.UndefOr[E]
  override type ClientList[E]   = js.Array[E]
  override type ClientMap[V]    = js.Dictionary[V]

  override type ClientFuture[T] = js.Promise[T]

  override type ClientLoader = ClientResourceLoader with ResourceLoader
  override type Loader       = ClientResourceLoader

  override protected def asClientOption[Internal, Client](
      from: Option[Internal],
      matcher: InternalClientMatcher[Internal, Client]): UndefOr[Client] =
    from.map(matcher.asClient).orUndefined

  override private[convert] def asClientList[A, B](from: Seq[A], matcher: InternalClientMatcher[A, B]): js.Array[B] =
    from.map(matcher.asClient).toJSArray

  override protected def asClientMap[Internal, Client](
      from: mutable.Map[String, Internal],
      matcher: InternalClientMatcher[Internal, Client]): Dictionary[Client] = {
    from.map { case (k, v) => k -> matcher.asClient(v) }.toJSDictionary
  }

  override protected def asInternalSeq[Client, Internal](
      from: js.Array[Client],
      matcher: ClientInternalMatcher[Client, Internal]): Seq[Internal] =
    from.toSeq.map(matcher.asInternal)

  override protected def asClientFuture[T](from: Future[T]): Promise[T] = from.toJSPromise

  override protected def asInternalFuture[Client, Internal](
      from: js.Promise[Client],
      matcher: ClientInternalMatcher[Client, Internal]): Future[Internal] =
    from.toFuture.map(matcher.asInternal)

  override protected def toScalaOption[E](from: UndefOr[E]): Option[E] = from.toOption

  override protected def toClientOption[E](from: Option[E]): ClientOption[E] = from.orUndefined
}
