package amf.core.remote

import amf.core.model.document.BaseUnit

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object GlobalCounter {
  var v = 0
}

class Cache {

  protected val cache: mutable.Map[String, Future[BaseUnit]] = mutable.Map()
  protected val dependencyGraph: mutable.Map[String, Set[String]] = mutable.Map()

  protected def addFromToEdge(from: String, to: String): Unit = {
    val fromNodes = dependencyGraph.getOrElse(to, Set())
    dependencyGraph.update(to, fromNodes.+(from))
  }

  protected def findCycles(node: String, acc: List[String] = List()): Option[List[String]] = {
    val fullPath: List[String] = acc ++ List(node)
    if (fullPath.size != fullPath.toSet.size) {
      Some(fullPath)
    } else {
      val sources = dependencyGraph.getOrElse(node, Set())
      val maybeCycles: Set[Option[List[String]]] = sources.map { source =>
        findCycles(source, fullPath)
      }
      maybeCycles.find(_.isDefined).flatten
    }
  }

  protected def beforeLast(elms: List[String]):Option[String] = {
    val lastTwo = elms.takeRight(2)
    if (lastTwo.size == 2) {
      lastTwo.headOption
    } else {
      None
    }
  }

  def getOrUpdate(url: String, context: Context)(supplier: () => Future[BaseUnit]): Future[BaseUnit] = synchronized {
    beforeLast(context.history) foreach { from =>
      addFromToEdge(from, url)
    }
    findCycles(url) match {
      case Some(_) =>
        if (cache(url).isCompleted) {
          cache(url)
        } else {
          cache.remove(url)
          supplier() map { res =>
            update(url, Future(res))
            res
          }
        }
      case _           =>
        cache.get(url) match {
          case Some(value) =>
            value
          case None =>
            val futureUnit = supplier()
            update(url, futureUnit)
            futureUnit
        }
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
