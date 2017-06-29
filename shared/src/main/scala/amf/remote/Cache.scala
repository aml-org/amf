package amf.remote

import amf.common.AMFAST

import scala.concurrent.Future

class Cache {

  private var cache: Map[String, Future[(AMFAST, Vendor)]] = Map[String, Future[(AMFAST, Vendor)]]()

  def getOrUpdate(url: String)(supplier: () => Future[(AMFAST, Vendor)]): Future[(AMFAST, Vendor)] = {
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
