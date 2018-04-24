package amf.core.remote

import amf.core.model.document.BaseUnit
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class Cache {

  protected var cache: Map[String, BaseUnit] = Map()

  def getOrUpdate(url: String)(supplier: () => Future[BaseUnit]): Future[BaseUnit] = {
    cache.get(url) match {
      case Some(value) => Future(value)
      case None =>
        supplier() map { value =>
          cache = cache + (url -> value)
          value
        }
    }
  }

  protected def size: Int = cache.size
}

object Cache {
  def apply(): Cache = new Cache()
}
