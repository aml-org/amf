package amf.remote

import amf.common.AMFAST

import scala.concurrent.Future

class Cache {
  protected var map: Map[String, Future[(AMFAST, Vendor)]] = Map[String, Future[(AMFAST, Vendor)]]()

  def exists(url: String): Boolean = {
    map.contains(url)
  }

  def getAST(url: String): Future[(AMFAST, Vendor)] = {
    map(url)
  }

  def update(url: String, amfAstV: Future[(AMFAST, Vendor)]): Unit = {
    map = map + (url -> amfAstV)
  }

}

object Cache {
  def apply(): Cache = new Cache()
}
