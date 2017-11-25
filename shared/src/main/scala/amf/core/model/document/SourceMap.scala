package amf.core.model.document

import amf.core.model.domain.AmfElement
import amf.core.parser.Value

import scala.collection.mutable

/**
  * Source maps for graph: Map(annotation -> Map(element -> value))
  */
class SourceMap(val annotations: mutable.ListMap[String, mutable.ListMap[String, String]]) {

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
    value.annotations
      .serializables()
      .foreach(a => {
        val tuple = element -> a.value
        annotations.get(a.name) match {
          case Some(values) => values += tuple
          case None         => annotations += (a.name -> mutable.ListMap(tuple))
        }
      })
  }

  def nonEmpty: Boolean = annotations.nonEmpty
}

object SourceMap {
  def apply(): SourceMap = new SourceMap(mutable.ListMap())

  def apply(id: String, element: AmfElement): SourceMap = {
    val map = mutable.ListMap[String, mutable.ListMap[String, String]]()
    element.annotations
      .serializables()
      .foreach(a => {
        map += (a.name -> mutable.ListMap(id -> a.value))
      })
    new SourceMap(map)
  }

  val empty: SourceMap = new SourceMap(mutable.ListMap.empty)
}
