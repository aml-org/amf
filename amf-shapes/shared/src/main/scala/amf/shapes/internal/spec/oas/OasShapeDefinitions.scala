package amf.shapes.internal.spec.oas

import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.parser.ShapeParserContext

object OasShapeDefinitions extends OasShapeDefinitions

trait OasShapeDefinitions {
  val oas2DefinitionsPrefix = "#/definitions/"

  val oas3DefinitionsPrefix = "#/components/schemas/"

  val oas3ComponentsPrefix = "#/components/"

  def stripDefinitionsPrefix(url: String)(implicit ctx: ShapeParserContext): String = {
    if (ctx.spec == Spec.OAS30 || ctx.spec == Spec.ASYNC20) url.stripPrefix(oas3DefinitionsPrefix)
    else url.stripPrefix(oas2DefinitionsPrefix)
  }

  def appendOas3ComponentsPrefix(url: String, fieldName: String): String = {
    appendPrefix(oas3ComponentsPrefix + s"$fieldName/", url)
  }

  def stripOas3ComponentsPrefix(url: String, fieldName: String): String =
    url.stripPrefix(oas3ComponentsPrefix + fieldName + "/")

  def appendSchemasPrefix(url: String, spec: Option[Spec] = None): String = spec match {
    case Some(Spec.OAS30) | Some(Spec.ASYNC20) =>
      if (!url.startsWith(oas3DefinitionsPrefix)) appendPrefix(oas3DefinitionsPrefix, url) else url
    case _ =>
      if (!url.startsWith(oas2DefinitionsPrefix)) appendPrefix(oas2DefinitionsPrefix, url) else url
  }

  protected def appendPrefix(prefix: String, url: String): String = prefix + url
}
