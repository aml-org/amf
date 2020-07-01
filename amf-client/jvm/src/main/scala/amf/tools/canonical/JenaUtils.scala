package amf.tools.canonical

import org.apache.jena.rdf.model.{ResIterator, Resource}

import scala.collection.mutable.ListBuffer

object JenaUtils {
  def all(iterator: ResIterator): Seq[Resource] = {
    val resources = new ListBuffer[Resource]
    while (iterator.hasNext) {
      resources += iterator.next()
    }
    resources.seq
  }
}
