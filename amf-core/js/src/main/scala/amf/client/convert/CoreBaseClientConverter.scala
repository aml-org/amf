package amf.client.convert

import amf.client.handler.{FileHandler, Handler, JsFileHandler, JsHandler}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

trait CoreBaseClientConverter extends CoreBaseConverter {

  override type ClientList[E] = js.Array[E]
  override type ClientMap[V]  = js.Dictionary[V]

  override type ClientFuture[T] = js.Promise[T]

  override type ClientResultHandler[T] = JsHandler[T] with Handler[T]
  override type ClientFileHandler      = JsFileHandler with FileHandler

  override private[amf] def asClientList[A, B](from: Seq[A], matcher: InternalClientMatcher[A, B]): js.Array[B] =
    from.map(matcher.asClient).toJSArray

  override private[convert] def asClientMap[Internal, Client](from: mutable.Map[String, Internal],
                                                              matcher: InternalClientMatcher[Internal, Client]) = {
    from.map { case (k, v) => k -> matcher.asClient(v) }.toJSDictionary
  }

  override private[convert] def asInternalSeq[Client, Internal](from: js.Array[Client],
                                                                matcher: ClientInternalMatcher[Client, Internal]) =
    from.toSeq.map(matcher.asInternal)

  override private[convert] def asClientFuture[T](from: Future[T]) = from.toJSPromise
}
