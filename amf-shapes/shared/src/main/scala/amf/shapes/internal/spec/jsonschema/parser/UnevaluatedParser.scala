package amf.shapes.internal.spec.jsonschema.parser

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.validation.core.ValidationSpecification
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.{ArrayShapeModel, NodeShapeModel}
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser.{QuickFieldParserOps, ShapeParserContext}
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{
  InvalidUnevaluatedItemsType,
  InvalidUnevaluatedPropertiesType
}
import org.yaml.model.{YMap, YType}

case class UnevaluatedInfo(
    key: String,
    booleanField: Field,
    schemaField: Field,
    error: ValidationSpecification,
    message: String
)

object UnevaluatedParser {
  val unevaluatedPropertiesInfo: UnevaluatedInfo = UnevaluatedInfo(
    "unevaluatedProperties",
    NodeShapeModel.UnevaluatedProperties,
    NodeShapeModel.UnevaluatedPropertiesSchema,
    InvalidUnevaluatedPropertiesType,
    "Invalid part type for unevaluated properties node. Should be a boolean or a map"
  )

  val unevaluatedItemsInfo: UnevaluatedInfo =
    UnevaluatedInfo(
      "unevaluatedItems",
      ArrayShapeModel.UnevaluatedItems,
      ArrayShapeModel.UnevaluatedItemsSchema,
      InvalidUnevaluatedItemsType,
      "Invalid part type for unevaluated items node. Should be a boolean or a map"
    )
}

class UnevaluatedParser(version: SchemaVersion, info: UnevaluatedInfo)(implicit ctx: ShapeParserContext)
    extends QuickFieldParserOps {

  private val UnevaluatedInfo(key, booleanField, schemaField, error, message) = info

  def parse(map: YMap, shape: AnyShape) {
    map.key(key).foreach { entry =>
      entry.value.tagType match {
        case YType.Bool => (booleanField in shape).explicit(entry)
        case YType.Map =>
          OasTypeParser(entry, s => Unit, version).parse().foreach { s =>
            shape.setWithoutId(booleanField, AmfScalar("true"), Annotations(SynthesizedField()))
            shape.setWithoutId(schemaField, s, Annotations(entry))
          }
        case _ =>
          ctx.eh.violation(error, shape, message, entry.location)
      }
    }
  }
}
