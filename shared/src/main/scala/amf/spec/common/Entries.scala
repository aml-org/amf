package amf.spec.common

import amf.common.core._
import amf.common.AMFAST

import scala.collection.immutable.ListMap
import scala.util.matching.Regex

case class Entries(ast: AMFAST) {
  def key(keyword: String): Option[EntryNode] = entries.get(keyword)

  def key(keyword: String, fn: (EntryNode => Unit)): Unit = key(keyword).foreach(fn)

  def regex(regex: String, fn: (Iterable[EntryNode] => Unit)): Unit = {
    val path: Regex = regex.r
    val values = entries
      .filterKeys({
        case path() => true
        case _      => false
      })
      .values
    if (values.nonEmpty) fn(values)
  }

  var entries: ListMap[String, EntryNode] = ListMap(ast.children.map(n => n.head.content.unquote -> EntryNode(n)): _*)

}
