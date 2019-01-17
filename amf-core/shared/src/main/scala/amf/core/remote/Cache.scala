package amf.core.remote

import amf.core.model.document.BaseUnit

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GlobalCounter {
  var v = 0;
}

class Cache {

  protected val cache: mutable.Map[String, Future[BaseUnit]] = mutable.Map()

  def getOrUpdate(url: String)(supplier: () => Future[BaseUnit]): Future[BaseUnit] = {
    cache.get(url) match {
      case Some(value) =>
        value
      case None =>
        val futureUnit = supplier()
        update(url, futureUnit)
        futureUnit
    }
  }

  private def update(url: String, value: Future[BaseUnit]): Unit = synchronized {
    cache.update(url, value)
  }

  protected def size: Int = cache.size
}

object Cache {
  def apply(): Cache = {
    new Cache()
  }
}
