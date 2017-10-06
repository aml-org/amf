package amf

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

    def key(keyword: String, fn: (YMapEntry => Unit)): Unit = key(keyword).foreach(fn)

    def regex(expression: String, fn: (Iterable[YMapEntry] => Unit)): Unit = {
      val entries = regex(expression)
      if (entries.nonEmpty) fn(entries)
    }

    def regex(expression: String): Iterable[YMapEntry] = {
      val path: Regex = expression.r
      map.entries.filter(entry => {
        entry.key.value match {
          case s: YScalar => path.unapplySeq(s.text).nonEmpty
          case _          => false
        }
      })
    }
  }

  implicit class YValueOps(value: YValue) {

    def toScalar: YScalar = value match {
      case s: YScalar => s
      case _          => throw new Exception(s"Expected scalar but found: $value")
    }

    def toMap: YMap = value match {
      case m: YMap => m
      case _       => throw new Exception(s"Expected map but found: $value")
    }

    def toSequence: YSequence = value match {
      case s: YSequence => s
      case _            => throw new Exception(s"Expected sequence but found: $value")
    }

    def asMap: Option[YMap] = value match {
      case m: YMap => Some(m)
      case _       => None
    }

    def asScalar: Option[YScalar] = value match {
      case s: YScalar => Some(s)
      case _          => None
    }
  }

  implicit class YScalarOps(value: YScalar) {}
}
