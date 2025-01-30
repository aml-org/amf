package amf.shapes.internal.spec.oas

import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.common.{JSONSchemaDraft201909SchemaVersion, JSONSchemaVersion}

object OasShapeDefinitions extends OasShapeDefinitions

trait OasShapeDefinitions {
  val oas2DefinitionsPrefix = "#/definitions/"

  val oas3DefinitionsPrefix = "#/components/schemas/"

  val oas3ComponentsPrefix = "#/components/"

  def stripDefinitionsPrefix(url: String)(implicit ctx: ShapeParserContext): String = {
    if (
      ctx.spec == Spec.OAS31 || ctx.spec == Spec.OAS30 || ctx.spec == Spec.ASYNC20 || ctx.spec == Spec.ASYNC21 || ctx.spec == Spec.ASYNC22 ||
      ctx.spec == Spec.ASYNC23 || ctx.spec == Spec.ASYNC24 || ctx.spec == Spec.ASYNC25 || ctx.spec == Spec.ASYNC26
    ) url.stripPrefix(oas3DefinitionsPrefix)
    else url.stripPrefix(oas2DefinitionsPrefix)
  }

  def appendOas3ComponentsPrefix(url: String, fieldName: String): String = {
    appendPrefix(oas3ComponentsPrefix + s"$fieldName/", url)
  }

  def stripOas3ComponentsPrefix(url: String, fieldName: String): String =
    url.stripPrefix(oas3ComponentsPrefix + fieldName + "/")

  def appendSchemasPrefix(
      url: String,
      spec: Option[Spec] = None,
      jsonSchemaVersion: Option[JSONSchemaVersion] = None
  ): String = spec match {
    case Some(Spec.OAS31) | Some(Spec.OAS30) | Some(Spec.ASYNC20) | Some(Spec.ASYNC21) | Some(Spec.ASYNC22) | Some(
          Spec.ASYNC23
        ) | Some(Spec.ASYNC24) | Some(Spec.ASYNC25) | Some(Spec.ASYNC26) =>
      if (!url.startsWith(oas3DefinitionsPrefix)) appendPrefix(oas3DefinitionsPrefix, url) else url
    case Some(Spec.JSONSCHEMA)
        if jsonSchemaVersion.exists(version => version.isBiggerThanOrEqualTo(JSONSchemaDraft201909SchemaVersion)) =>
      if (!url.startsWith("#/$defs/")) appendPrefix("#/$defs/", url) else url
    case _ =>
      if (!url.startsWith(oas2DefinitionsPrefix)) appendPrefix(oas2DefinitionsPrefix, url) else url
  }

  protected def appendPrefix(prefix: String, url: String): String = prefix + url
}
