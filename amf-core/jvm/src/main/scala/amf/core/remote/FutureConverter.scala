package amf.core.remote

import java.util.concurrent.CompletableFuture

import scala.compat.java8.FutureConverters
import scala.concurrent.Future
import scala.language.implicitConversions

/**
  *
  */
//todo delete
object FutureConverter {
  implicit def converters(s: Future[_]): FutureConverter = new FutureConverter(s)
}

class FutureConverter(future: Future[_]) {
  def asJava[T]: CompletableFuture[T] =
    FutureConverters.toJava(future).toCompletableFuture.asInstanceOf[CompletableFuture[T]]

}
