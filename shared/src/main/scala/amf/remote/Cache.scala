package amf.remote

import amf.common.AMFAST

import scala.concurrent.Future

class Cache {
  protected var map: Map[String, Future[AMFAST]] = Map[String, Future[AMFAST]]()

  def exists(url: String): Boolean = {
    map.contains(url)
  }

  def getAST(url: String): Future[AMFAST] = {
    map(url)
  }

  def update(url: String, amfAst: Future[AMFAST]): Unit = {
    map = map + (url -> amfAst)
  }

}

object Cache {
  def apply(): Cache = new Cache()
}
