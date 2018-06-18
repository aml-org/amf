package amf.core.utils

import scala.annotation.tailrec

/**
  * Topological sort
  */
object TSort {

  def tsort[A](edges: Traversable[(A, A)]): Iterable[A] = {

    val preds = edges.foldLeft(Map[A, Set[A]]()) { (acc, e) =>
      acc + (e._1 -> acc.getOrElse(e._1, Set())) + (e._2 -> (acc.getOrElse(e._2, Set()) + e._1))
    }

    tsort(preds, Seq())
  }

  @tailrec def tsort[A](preds: Map[A, Set[A]], done: Iterable[A]): Iterable[A] = {
    val (noPreds, hasPreds) = preds.partition { _._2.isEmpty }
    if (noPreds.isEmpty) {
      if (hasPreds.isEmpty) done else throw new RuntimeException(hasPreds.toString)
    } else {
      val found = noPreds.keys
      tsort(hasPreds.mapValues { _ -- found }, done ++ found)
    }
  }

}
