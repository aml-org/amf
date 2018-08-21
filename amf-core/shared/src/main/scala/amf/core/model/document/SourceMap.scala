package amf.core.model.document

import amf.core.model.domain.AmfElement
import amf.core.parser.Value

import scala.collection.mutable

/**
  * Source maps for graph: Map(annotation -> Map(element -> value))
  */
class SourceMap(val annotations: mutable.ListMap[String, mutable.ListMap[String, String]],
                val eternals: mutable.ListMap[String, mutable.ListMap[String, String]]) {

  def annotation(annotation: String): (String, String) => Unit = {
    val map = annotations.get(annotation).orElse(eternals.get(annotation)) match {
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
    value.annotations
      .eternals()
      .foreach(e => {
        val tuple = element -> e.value
        eternals.get(e.name) match {
          case Some(values) => values += tuple
          case None         => eternals += (e.name -> mutable.ListMap(tuple))
        }
      })
  }

  def all(): mutable.ListMap[String, mutable.ListMap[String, String]] =
    (annotations ++ eternals).asInstanceOf[mutable.ListMap[String, mutable.ListMap[String, String]]]

  def nonEmpty: Boolean = annotations.nonEmpty

  def serializablesNonEmpty: Boolean = annotations.nonEmpty
}

object SourceMap {
  def apply(): SourceMap = new SourceMap(mutable.ListMap(), new mutable.ListMap())

  def apply(id: String, element: AmfElement): SourceMap = {
    val map = SourceMap()
    element.annotations
      .serializables()
      .foreach(a => {
        map.annotations += (a.name -> mutable.ListMap(id -> a.value))
      })

    element.annotations
      .eternals()
      .foreach(e => {
        map.eternals += (e.name -> mutable.ListMap(id -> e.value))
      })
    map
  }

  val empty: SourceMap = new SourceMap(mutable.ListMap.empty, mutable.ListMap.empty)
}
