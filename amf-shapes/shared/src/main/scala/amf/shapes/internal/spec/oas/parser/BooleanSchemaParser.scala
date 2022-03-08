package amf.shapes.internal.spec.oas.parser

import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.BooleanSchema
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.{JSONSchemaDraft6SchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidBooleanSchemaForSchemaVersion
import org.yaml.model.YNode
import org.yaml.model.YNodeLike.toBoolean

object BooleanSchemaParser {
  def apply(entryOrNode: YMapEntryLike, node: YNode, version: SchemaVersion,
  )(implicit ctx: ShapeParserContext): Option[AnyShape] = {
    if (version isBiggerThanOrEqualTo JSONSchemaDraft6SchemaVersion) {
      mapSchemaToEquivalent(entryOrNode)
    } else {
      throwInvalidDraftVersion(node)
      None
    }
  }

  private def mapSchemaToEquivalent(entryOrNode: YMapEntryLike) = {
    val shape = AnyShape(entryOrNode.ast)
    if (valueIsFalse(entryOrNode)) mapFalseToRejectAllSchema(shape)
    shape.annotations += BooleanSchema()
    Option(shape)
  }

  private def mapFalseToRejectAllSchema(shape: AnyShape) = {
    shape.setWithoutId(ShapeModel.Not, AnyShape(), Annotations.virtual())
  }

  private def throwInvalidDraftVersion(node: YNode)(implicit ctx: ShapeParserContext): Unit = {
    ctx.eh.violation(InvalidBooleanSchemaForSchemaVersion,
      node.toString,
      "Boolean schemas not supported in JSON Schema below version draft-6",
      node.location)
  }

  private def valueIsFalse(entryOrNode: YMapEntryLike) = !toBoolean(entryOrNode.value)
}
