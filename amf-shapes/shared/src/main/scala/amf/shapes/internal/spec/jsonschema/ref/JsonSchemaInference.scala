package amf.shapes.internal.spec.jsonschema.ref

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.shapes.internal.spec.common._
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidJsonSchemaVersion
import org.yaml.model.{YMap, YNode, YScalar}

trait JsonSchemaInference {

  val defaultSchemaVersion: SchemaVersion

  def parseSchemaVersion(ast: YNode, errorHandler: AMFErrorHandler): SchemaVersion =
    parseSchemaEntry(ast) match {
      case Some(node) =>
        tryParserVersion(node)(errorHandler) match {
          case Some(version) => getSchemaVersionFromString(version).getOrElse(defaultSchemaVersion)
          case None          => defaultSchemaVersion
        }
      case None => defaultSchemaVersion
    }

  def getSchemaVersionFromString(text: String): Option[JSONSchemaVersion] = {
    normalize(text) match {
      case JSONSchemaDraft3SchemaVersion.url      => Some(JSONSchemaDraft3SchemaVersion)
      case JSONSchemaDraft4SchemaVersion.url      => Some(JSONSchemaDraft4SchemaVersion)
      case JSONSchemaDraft6SchemaVersion.url      => Some(JSONSchemaDraft6SchemaVersion)
      case JSONSchemaDraft7SchemaVersion.url      => Some(JSONSchemaDraft7SchemaVersion)
      case JSONSchemaDraft201909SchemaVersion.url => Some(JSONSchemaDraft201909SchemaVersion)
      case _                                      => None
    }
  }

  def normalize(text: String): String = {
    val normalizedScheme = text.replace("https", "http")
    if (normalizedScheme.endsWith("#")) normalizedScheme
    else normalizedScheme + "#"
  }

  private def parseSchemaEntry(ast: YNode): Option[YNode] = {
    ast.value match {
      case map: YMap => map.map.get("$schema")
      case _         => None
    }
  }

  private def tryParserVersion(node: YNode)(errorHandler: AMFErrorHandler): Option[String] = {
    node.value match {
      case scalar: YScalar => Some(scalar.text)
      case _ =>
        errorHandler.violation(
          InvalidJsonSchemaVersion,
          "",
          "JSON Schema version value must be a string",
          node.location
        )
        None
    }
  }
}
