package amf.shapes.internal.spec.oas.parser.field

import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.vocabulary.Namespace.Data
import amf.core.internal.annotations.ExplicitField
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.{YMapOps, YNodeLikeOps}
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.parser.domain.Annotations.{inferred, synthesized}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.XMLSerializerModel.Namespace
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.common.{JSONSchemaDraft3SchemaVersion, OAS20SchemaVersion, SchemaVersion}
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{
  InvalidRequiredBooleanForSchemaVersion,
  ReadOnlyPropertyMarkedRequired
}
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YType}

import scala.util.Try

case class PropertiesParser(
    map: YMap,
    version: SchemaVersion,
    producer: (String, Annotations) => PropertyShape,
    requiredFields: Map[String, YNode],
    patterned: Boolean = false
)(implicit ctx: ShapeParserContext) {
  def parse(): Seq[PropertyShape] = {
    map.entries.map(entry => PropertyShapeParser(entry, version, producer, requiredFields, patterned).parse())
  }
}

case class PropertyShapeParser(
    entry: YMapEntry,
    version: SchemaVersion,
    producer: (String, Annotations) => PropertyShape,
    requiredFields: Map[String, YNode],
    patterned: Boolean
)(implicit ctx: ShapeParserContext) {

  def parse(): PropertyShape = {

    val name            = entry.key.as[YScalar].text
    val nameAnnotations = Annotations(entry.key)
    val required        = requiredFields.contains(name)
    val requiredAnnotations =
      requiredFields.get(name).map(node => Annotations(node)).getOrElse(synthesized())
    val property = producer(name, nameAnnotations)
      .add(Annotations(entry))
      .setWithoutId(
        PropertyShapeModel.MinCount,
        AmfScalar(if (required) 1 else 0, synthesized()),
        requiredAnnotations += ExplicitField()
      )

    property.setWithoutId(
      PropertyShapeModel.Path,
      AmfScalar((Data + entry.key.as[YScalar].text.urlComponentEncoded).iri(), Annotations(entry.key)),
      inferred()
    )

    if (version.isInstanceOf[OAS20SchemaVersion])
      validateReadOnlyAndRequired(entry.value.toOption[YMap], property, required)

    // This comes from JSON Schema draft-3, we will parse it for backward compatibility but we will not generate it
    entry.value
      .toOption[YMap]
      .foreach(
        _.key(
          "required",
          entry => {
            if (entry.value.tagType == YType.Bool) {
              if (version != JSONSchemaDraft3SchemaVersion) {
                ctx.eh.warning(
                  InvalidRequiredBooleanForSchemaVersion,
                  property,
                  "Required property boolean value is only supported in JSON Schema draft-3",
                  entry.location
                )
              }
              val required =
                amf.core.internal.parser.domain.ScalarNode(entry.value).boolean().value.asInstanceOf[Boolean]
              property.setWithoutId(
                PropertyShapeModel.MinCount,
                AmfScalar(if (required) 1 else 0),
                synthesized()
              )
            }
          }
        )
      )

    var shape = AnyShape()
    OasTypeParser(entry, shape => Unit, version).parse().foreach(shape = _)
    property.setWithoutId(PropertyShapeModel.Range, shape, Annotations.inferred())

    if (patterned) property.withPatternName(name)

    property
  }

  private def validateReadOnlyAndRequired(map: Option[YMap], property: PropertyShape, isRequired: Boolean): Unit = {
    map.foreach(
      _.key(
        "readOnly",
        readOnlyEntry => {
          val readOnly = Try(readOnlyEntry.value.as[YScalar].text.toBoolean).getOrElse(false)
          if (readOnly && isRequired) {
            ctx.eh.warning(
              ReadOnlyPropertyMarkedRequired,
              property,
              "Read only property should not be marked as required by a schema",
              readOnlyEntry.location
            )
          }
        }
      )
    )
  }
}
