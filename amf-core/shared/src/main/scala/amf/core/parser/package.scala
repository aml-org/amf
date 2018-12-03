package amf.core

import org.yaml.convert.YRead
import org.yaml.convert.YRead._
import org.yaml.model._

import scala.util.matching.Regex

package object parser {

  implicit class YMapOps(map: YMap) {

    def key(keyword: String): Option[YMapEntry] =
      map.entries.find(entry => {
        entry.key.value match {
          case s: YScalar => s.text == keyword
          case _          => false
        }
      })

    def key(keyword: String, fn: YMapEntry => Unit): Unit = key(keyword).foreach(fn)

    def regex(expression: String, fn: Iterable[YMapEntry] => Unit): Unit = {
      val entries = regex(expression)
      if (entries.nonEmpty) fn(entries)
    }

    def regex(expression: String): Iterable[YMapEntry] = {
      val path: Regex = expression.r
      map.entries.filter { entry =>
        entry.key.value match {
          case s: YScalar => path.unapplySeq(s.text).nonEmpty
          case _          => false
        }
      }
    }
  }

  implicit object YScalarYRead extends YRead[YScalar] {
    def read(node: YNode): Either[YError, YScalar] = node.value match {
      case s: YScalar => Right(s)
      case other      => error(node, s"Expected scalar but found: $other")
    }
    override def defaultValue: YScalar = YScalar.Null
  }

  implicit class YNodeLikeOps(node: YNodeLike) {
    def toOption[T](implicit conversion: YRead[T]): Option[T] = node.to[T].toOption
  }

}
