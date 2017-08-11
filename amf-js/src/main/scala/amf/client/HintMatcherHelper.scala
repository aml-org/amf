package amf.client

import amf.remote.Syntax.{Json, Syntax, Yaml}
import amf.remote._

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

/**
  *
  */
@JSExportTopLevel("HintMatcherHelper")
object HintMatcherHelper {

  @JSExport
  def matchSourceHint(source: String): Hint = {
    source match {
      case "json" | "oas" | "openapi" => OasJsonHint
      case "raml" | "yaml"            => RamlYamlHint
      case _                          => AmfJsonLdHint
    }
  }

  @JSExport
  def matchToVendor(toVendor: String): Vendor = {
    toVendor match {
      case "json" | "oas" | "openapi" => Oas
      case "raml" | "yaml"            => Raml
      case _                          => Amf
    }
  }

  @JSExport
  def matchToSyntax(syntax: String): Syntax = {
    syntax match {
      case "json" | "openapi" => Json
      case "raml" | "yaml"    => Yaml
      case _                  => Json
    }
  }
}
