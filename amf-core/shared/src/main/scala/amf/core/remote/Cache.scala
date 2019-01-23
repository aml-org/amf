package amf.core.remote

import amf.core.model.document.BaseUnit

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GlobalCounter {
  var v = 0;
}

class Cache {

  protected val cache: mutable.Map[String, BaseUnit] = mutable.Map()

  def getOrUpdate(url: String)(supplier: () => Future[BaseUnit]): Future[BaseUnit] = {
    //println(s"SIZE ${cache.size} :: ${System.identityHashCode(this)}")
    cache.get(url) match {
      case Some(value) =>
        //println(s"- ${url} HIT ${System.identityHashCode(this)}")
        Future(value)
      case None =>
        //println(s"- ${url} MISS ${System.identityHashCode(this)}")
        supplier() map { value =>
          update(url, value)
          value
        }
    }
  }

  private def update(url: String, value: BaseUnit): Unit = synchronized {
    cache.update(url, value)
  }

  protected def size: Int = cache.size
}

object Cache {
  def apply(): Cache = {
    new Cache()
  }
}
