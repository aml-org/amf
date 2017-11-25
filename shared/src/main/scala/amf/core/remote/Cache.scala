package amf.core.remote

import amf.core.model.document.BaseUnit

import scala.concurrent.Future

class Cache {

  private var cache: Map[String, Future[BaseUnit]] = Map()

  def getOrUpdate(url: String)(supplier: () => Future[BaseUnit]): Future[BaseUnit] = {
    cache.get(url) match {
      case Some(value) => value
      case None =>
        val value = supplier()
        cache = cache + (url -> value)
        value
    }
  }

  protected def size: Int = cache.size
}

object Cache {
  def apply(): Cache = new Cache()
}
