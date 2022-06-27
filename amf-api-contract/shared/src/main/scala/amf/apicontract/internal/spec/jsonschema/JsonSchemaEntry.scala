package amf.apicontract.internal.spec.jsonschema

import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.{Root, YMapOps, YNodeLikeOps}
import amf.shapes.internal.spec.common._
import amf.shapes.internal.spec.jsonschema.emitter.JsonSchemaEntryEmitter
import amf.shapes.internal.spec.jsonschema.ref.JsonSchemaInference
import org.yaml.model.YMap

/** */

object JsonSchemaEntry extends JsonSchemaInference {

  private val schemaKey = "$schema"

  def apply(root: Root): Option[JSONSchemaVersion] =
    root.parsed match {
      case parsed: SyamlParsedDocument =>
        parsed.document.to[YMap] match {
          case Right(map) =>
            map
              .key(schemaKey)
              .flatMap(extension => JsonSchemaEntry(extension.value.toOption[String].getOrElse("")))
          case Left(_) => None
        }
      case _ => None
    }

  def apply(text: String): Option[JSONSchemaVersion] = {
    val toMatch = normalize(text)
    toMatch match {
      case JSONSchemaDraft3SchemaVersion.url      => Some(JSONSchemaDraft3SchemaVersion)
      case JSONSchemaDraft4SchemaVersion.url      => Some(JSONSchemaDraft4SchemaVersion)
      case JSONSchemaDraft6SchemaVersion.url      => Some(JSONSchemaDraft6SchemaVersion)
      case JSONSchemaDraft7SchemaVersion.url      => Some(JSONSchemaDraft7SchemaVersion)
      case JSONSchemaDraft201909SchemaVersion.url => Some(JSONSchemaDraft201909SchemaVersion)
      case _                                      => None
    }
  }

  private def normalize(text: String): String = {
    val normalizedScheme = text.replace("https", "http")
    if (normalizedScheme.endsWith("#")) normalizedScheme
    else normalizedScheme + "#"
  }

  override val defaultSchemaVersion: SchemaVersion = JSONSchemaUnspecifiedVersion
}
