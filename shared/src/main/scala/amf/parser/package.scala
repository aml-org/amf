package amf

import org.yaml.model.{YMap, YMapEntry, YScalar, YValue}

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
  }

  implicit class YValueOps(value: YValue) {

    def scalar: YScalar = value match {
      case s: YScalar => s
      case _          => throw new Exception(s"Expected scalar but found: $value")
    }

    def map: YMap = value match {
      case m: YMap => m
      case _       => throw new Exception(s"Expected map but found: $value")
    }
  }

  implicit class YScalarOps(value: YScalar) {

  }
}
