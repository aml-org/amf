package amf.shapes.internal.spec.oas.parser

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.parser.{YMapOps, YNodeLikeOps}
import amf.shapes.internal.spec.common.TypeDef._
import amf.shapes.internal.spec.OasTypeDefMatcher.matchType
import amf.shapes.internal.spec.common.TypeDef
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidJsonSchemaType
import org.yaml.model.{YMap, YScalar}

object TypeDetector {
  def detect(map: YMap)(implicit errorHandler: AMFErrorHandler): Option[TypeDef] = {

    val detectionCriteria = ExplicitTypeCriteria()
      .chain(ObjectCriteria)
      .chain(AmfUnionCriteria)
      .chain(ArrayCriteria)
      .chain(NumberCriteria)
      .chain(StringCriteria)

    detectionCriteria.detect(map)
  }

  abstract class TypeCriteria {
    def detect(map: YMap): Option[TypeDef]
    def chain(criteria: TypeCriteria): TypeCriteria = ChainedCriteria(this, criteria)
  }

  case class ChainedCriteria(first: TypeCriteria, second: TypeCriteria) extends TypeCriteria {
    override def detect(map: YMap): Option[TypeDef] = first.detect(map).orElse(second.detect(map))
  }

  case class ExplicitTypeCriteria()(implicit val errorHandler: AMFErrorHandler) extends TypeCriteria {
    override def detect(map: YMap): Option[TypeDef] = map.key("type").flatMap { e =>
      val typeText          = e.value.as[YScalar].text
      val formatTextOrEmpty = map.key("format").flatMap(e => e.value.toOption[YScalar].map(_.text)).getOrElse("")
      val result            = matchType(typeText, formatTextOrEmpty, UndefinedType)
      if (result == UndefinedType) {
        errorHandler.violation(InvalidJsonSchemaType, "", s"Invalid type $typeText", e.value)
        None
      } else Some(result)
    }
  }

  object AmfUnionCriteria extends TypeCriteria {
    override def detect(map: YMap): Option[TypeDef] = map.key("x-amf-union").map(_ => UnionType)
  }

  object StringCriteria extends TypeCriteria {
    override def detect(map: YMap): Option[TypeDef] =
      map
        .key("minLength")
        .orElse(map.key("maxLength"))
        .orElse(map.key("pattern"))
        .orElse(map.key("format"))
        .map(_ => StrType)
  }

  object NumberCriteria extends TypeCriteria {
    override def detect(map: YMap): Option[TypeDef] =
      map
        .key("multipleOf")
        .orElse(map.key("minimum"))
        .orElse(map.key("maximum"))
        .orElse(map.key("exclusiveMinimum"))
        .orElse(map.key("exclusiveMaximum"))
        .map(_ => NumberType)
  }

  object ObjectCriteria extends TypeCriteria {
    override def detect(map: YMap): Option[TypeDef] =
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
  }

  object LinkCriteria extends TypeCriteria {
    override def detect(map: YMap): Option[TypeDef] = map.key("$ref").map(_ => LinkType)
  }

  object ArrayCriteria extends TypeCriteria {
    override def detect(map: YMap): Option[TypeDef] =
      map
        .key("items")
        .orElse(map.key("minItems"))
        .orElse(map.key("maxItems"))
        .orElse(map.key("uniqueItems"))
        .map(_ => ArrayType)
  }
}
