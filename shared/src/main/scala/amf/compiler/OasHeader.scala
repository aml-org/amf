package amf.compiler

import amf.parser.{YMapOps, YValueOps}

/**
  * Created by hernan.najles on 9/20/17.
  */
class OasHeader private[compiler] (val key: String, val value: String) {}

object OasHeader {

  val Oas_20: OasHeader = new OasHeader("swagger", "2.0")

  def apply(root: Root): Option[OasHeader] = {
    for {
      map     <- root.document.value.flatMap(_.asMap)
      value   <- map.key("swagger").map(_.value.value)
      version <- value.asScalar.map(_.text) if version.equals("2.0")
    } yield Oas_20
  }
}
