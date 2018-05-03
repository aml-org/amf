package amf.core.remote

import amf.core.model.document.BaseUnit

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Cache {

  protected val cache: mutable.Map[String, BaseUnit] = mutable.Map()

  def getOrUpdate(url: String)(supplier: () => Future[BaseUnit]): Future[BaseUnit] = {
    cache.get(url) match {
      case Some(value) =>
        Future(value)
      case None =>
        supplier() map { value =>
           cache.update(url, value)
          value
        }
    }
  }

  protected def size: Int = cache.size
}

object Cache {
  def apply(): Cache = new Cache()
}
