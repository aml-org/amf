package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.errorhandling.ErrorHandler
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft201909SchemaVersion,
  JSONSchemaDraft3SchemaVersion,
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaDraft6SchemaVersion,
  JSONSchemaDraft7SchemaVersion,
  JSONSchemaUnspecifiedVersion,
  JSONSchemaVersion,
  SchemaVersion
}
import amf.validations.ParserSideValidations.InvalidJsonSchemaVersion
import org.yaml.model.{YMap, YNode, YScalar}

trait JsonSchemaInference {

  val defaultSchemaVersion: JSONSchemaVersion

  def parseSchemaVersion(ast: YNode, errorHandler: ErrorHandler): SchemaVersion =
    parseSchemaEntry(ast) match {
      case Some(node) =>
        tryParserVersion(node)(errorHandler) match {
          case Some(version) => getVersionFor(version).getOrElse(defaultSchemaVersion)
          case None          => defaultSchemaVersion
        }
      case None => JSONSchemaUnspecifiedVersion
    }

  private def parseSchemaEntry(ast: YNode): Option[YNode] = {
    ast.value match {
      case map: YMap => map.map.get("$schema")
      case _         => None
    }
  }

  private def tryParserVersion(node: YNode)(errorHandler: ErrorHandler): Option[String] = {
    node.value match {
      case scalar: YScalar => Some(adaptInput(scalar.text))
      case _ =>
        thowUnexpectedVersionNodeType(node, errorHandler)
        None
    }
  }

  private def thowUnexpectedVersionNodeType(node: YNode, errorHandler: ErrorHandler): Unit = {
    errorHandler.violation(InvalidJsonSchemaVersion, "", "JSON Schema version value must be a string", node)
  }

  private def adaptInput(schema: String): String = schema.lastOption match {
    case Some('#') => schema
    case _      => s"${schema}#"
  }

  private def getVersionFor(schema: String): Option[JSONSchemaVersion] = mappings.get(schema)

  private lazy val mappings = Map(
    "http://json-schema.org/draft-01/schema#" -> JSONSchemaDraft3SchemaVersion,
    "http://json-schema.org/draft-02/schema#" -> JSONSchemaDraft3SchemaVersion,
    JSONSchemaDraft3SchemaVersion.url         -> JSONSchemaDraft3SchemaVersion,
    JSONSchemaDraft4SchemaVersion.url         -> JSONSchemaDraft4SchemaVersion,
    JSONSchemaDraft6SchemaVersion.url         -> JSONSchemaDraft6SchemaVersion,
    JSONSchemaDraft7SchemaVersion.url         -> JSONSchemaDraft7SchemaVersion,
    JSONSchemaDraft201909SchemaVersion.url    -> JSONSchemaDraft201909SchemaVersion
  )
}
