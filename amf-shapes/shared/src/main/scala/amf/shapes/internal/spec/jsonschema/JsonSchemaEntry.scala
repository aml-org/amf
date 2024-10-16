package amf.shapes.internal.spec.jsonschema

import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.{Root, YMapOps, YNodeLikeOps}
import amf.shapes.internal.spec.common._
import amf.shapes.internal.spec.jsonschema.ref.JsonSchemaInference
import org.yaml.model.YMap

object JsonSchemaEntry extends JsonSchemaInference {
  override val defaultSchemaVersion: SchemaVersion = JSONSchemaUnspecifiedVersion

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

  def apply(text: String): Option[JSONSchemaVersion] = getSchemaVersionFromString(text)
}
