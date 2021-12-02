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
          case Some(version) => getVersionFor(version).getOrElse(defaultSchemaVersion)
          case None          => defaultSchemaVersion
        }
      case None => defaultSchemaVersion
    }

  private def parseSchemaEntry(ast: YNode): Option[YNode] = {
    ast.value match {
      case map: YMap => map.map.get("$schema")
      case _         => None
    }
  }

  private def tryParserVersion(node: YNode)(errorHandler: AMFErrorHandler): Option[String] = {
    node.value match {
      case scalar: YScalar => Some(adaptInput(scalar.text))
      case _ =>
        thowUnexpectedVersionNodeType(node, errorHandler)
        None
    }
  }

  private def thowUnexpectedVersionNodeType(node: YNode, errorHandler: AMFErrorHandler): Unit = {
    errorHandler.violation(InvalidJsonSchemaVersion, "", "JSON Schema version value must be a string", node.location)
  }

  private def adaptInput(schema: String): String = schema.lastOption match {
    case Some('#') => schema
    case _         => s"${schema}#"
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
