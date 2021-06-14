package amf.plugins.document.apicontract.parser.spec

import amf.core.internal.remote.Vendor
import amf.plugins.document.apicontract.parser.ShapeParserContext

object OasShapeDefinitions extends OasShapeDefinitions

trait OasShapeDefinitions {
  val oas2DefinitionsPrefix = "#/definitions/"

  val oas3DefinitionsPrefix = "#/components/schemas/"

  val oas3ComponentsPrefix = "#/components/"

  def stripDefinitionsPrefix(url: String)(implicit ctx: ShapeParserContext): String = {
    if (ctx.vendor == Vendor.OAS30 || ctx.vendor == Vendor.ASYNC20) url.stripPrefix(oas3DefinitionsPrefix)
    else url.stripPrefix(oas2DefinitionsPrefix)
  }

  def appendOas3ComponentsPrefix(url: String, fieldName: String): String = {
    appendPrefix(oas3ComponentsPrefix + s"$fieldName/", url)
  }

  def stripOas3ComponentsPrefix(url: String, fieldName: String): String =
    url.stripPrefix(oas3ComponentsPrefix + fieldName + "/")

  def appendSchemasPrefix(url: String, vendor: Option[Vendor] = None): String = vendor match {
    case Some(Vendor.OAS30) | Some(Vendor.ASYNC20) =>
      if (!url.startsWith(oas3DefinitionsPrefix)) appendPrefix(oas3DefinitionsPrefix, url) else url
    case _ =>
      if (!url.startsWith(oas2DefinitionsPrefix)) appendPrefix(oas2DefinitionsPrefix, url) else url
  }

  protected def appendPrefix(prefix: String, url: String): String = prefix + url
}
