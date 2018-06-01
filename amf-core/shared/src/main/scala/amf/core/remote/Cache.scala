package amf.core.remote

import amf.core.model.document.BaseUnit

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Cache {

  protected val cache: mutable.Map[String, BaseUnit] = mutable.Map()

  def getOrUpdate(url: String)(supplier: () => Future[BaseUnit]): Future[BaseUnit] = cache.get(url) match {
    case Some(value) =>
      Future(value)
    case None =>
      supplier() map { value =>
        update(url, value)
        value
      }
  }

  private def update(url: String, value: BaseUnit): Unit = synchronized {
    cache.update(url, value)
  }

  protected def size: Int = cache.size
}

object Cache {
  def apply(): Cache = new Cache()
}
