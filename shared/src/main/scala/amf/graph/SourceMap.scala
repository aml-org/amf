package amf.graph

import amf.domain.Value
import amf.model.AmfElement

import scala.collection.mutable

/**
  * Source maps for graph: Map(annotation -> Map(element -> value))
  */
private class SourceMap(val annotations: mutable.ListMap[String, mutable.ListMap[String, String]]) {

  def annotation(annotation: String): (String, String) => Unit = {
    val map = annotations.get(annotation) match {
      case Some(values) => values
      case None =>
        val values = mutable.ListMap[String, String]()
        annotations += (annotation -> values)
        values
    }
    map.update
  }

  def property(element: String)(value: Value): Unit = {
    value.annotations.foreach(a => {
      val tuple = element -> a.value
      annotations.get(a.name) match {
        case Some(values) => values += tuple
        case None         => annotations += (a.name -> mutable.ListMap(tuple))
      }
    })
  }

  def nonEmpty: Boolean = annotations.nonEmpty
}

private object SourceMap {
  def apply(): SourceMap = new SourceMap(mutable.ListMap())

  def apply(id: String, element: AmfElement): SourceMap = {
    val map = mutable.ListMap[String, mutable.ListMap[String, String]]()
    element.annotations.foreach(a => {
      map += (a.name -> mutable.ListMap(id -> a.value))
    })
    new SourceMap(map)
  }

  val empty: SourceMap = new SourceMap(mutable.ListMap.empty)
}
