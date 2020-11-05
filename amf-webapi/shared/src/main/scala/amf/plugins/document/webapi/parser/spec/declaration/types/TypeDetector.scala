package amf.plugins.document.webapi.parser.spec.declaration.types

import amf.core.errorhandling.ErrorHandler
import amf.core.parser.{YMapOps, YNodeLikeOps}
import amf.plugins.document.webapi.parser.OasTypeDefMatcher.matchType
import amf.plugins.document.webapi.parser.spec.declaration.{OASSchemaVersion, SchemaVersion}
import amf.plugins.domain.shapes.models.TypeDef
import amf.plugins.domain.shapes.models.TypeDef.{AnyType, ArrayType, LinkType, NumberType, ObjectType, StrType, UndefinedType, UnionType}
import amf.validations.ParserSideValidations.InvalidJsonSchemaType
import org.yaml.model.{YMap, YScalar}

object TypeDetector {
  def detect(version: SchemaVersion, map: YMap)(implicit errorHandler: ErrorHandler): TypeDef = {
    val defaultType = version match {
      case oasSchema: OASSchemaVersion if oasSchema.position == "parameter" => UndefinedType
      case _                                                                => AnyType
    }

    detectDependency(map)
      .orElse(detectType(map))
      .orElse(detectObjectProperties(map))
      .orElse(detectAmfUnion(map))
      .orElse(detectItemProperties(map))
      .orElse(detectNumberProperties(map))
      .orElse(detectStringProperties(map))
      .getOrElse(defaultType)
  }

  private def detectObjectProperties(map: YMap): Option[TypeDef.ObjectType.type] =
    map
      .key("properties")
      .orElse(map.key("x-amf-merge"))
      .orElse(map.key("minProperties"))
      .orElse(map.key("maxProperties"))
      .orElse(map.key("dependencies"))
      .orElse(map.key("patternProperties"))
      .orElse(map.key("additionalProperties"))
      .orElse(map.key("discriminator"))
      .map(_ => ObjectType)

  private def detectItemProperties(map: YMap): Option[TypeDef.ArrayType.type] =
    map
      .key("items")
      .orElse(map.key("minItems"))
      .orElse(map.key("maxItems"))
      .orElse(map.key("uniqueItems"))
      .map(_ => ArrayType)

  private def detectNumberProperties(map: YMap): Option[TypeDef.NumberType.type] =
    map
      .key("multipleOf")
      .orElse(map.key("minimum"))
      .orElse(map.key("maximum"))
      .orElse(map.key("exclusiveMinimum"))
      .orElse(map.key("exclusiveMaximum"))
      .map(_ => NumberType)

  private def detectStringProperties(map: YMap): Option[TypeDef.StrType.type] =
    map
      .key("minLength")
      .orElse(map.key("maxLength"))
      .orElse(map.key("pattern"))
      .orElse(map.key("format"))
      .map(_ => StrType)

  private def detectDependency(map: YMap): Option[TypeDef] = map.key("$ref").map(_ => LinkType)

  private def detectAmfUnion(map: YMap): Option[TypeDef.UnionType.type] = map.key("x-amf-union").map(_ => UnionType)

  private def detectType(map: YMap)(implicit errorHandler: ErrorHandler): Option[TypeDef] = map.key("type").flatMap { e =>
    val typeText          = e.value.as[YScalar].text
    val formatTextOrEmpty = map.key("format").flatMap(e => e.value.toOption[YScalar].map(_.text)).getOrElse("")
    val result            = matchType(typeText, formatTextOrEmpty, UndefinedType)
    if (result == UndefinedType) {
      errorHandler.violation(InvalidJsonSchemaType, "", s"Invalid type $typeText", e.value)
      None
    } else Some(result)
  }

}
