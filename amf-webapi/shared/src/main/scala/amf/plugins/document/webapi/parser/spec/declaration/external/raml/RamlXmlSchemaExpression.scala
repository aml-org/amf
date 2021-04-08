package amf.plugins.document.webapi.parser.spec.declaration.external.raml

import amf.core.annotations.{ExternalFragmentRef, LexicalInformation}
import amf.core.metamodel.domain.{ExternalSourceElementModel, ShapeModel}
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, Range, ReferenceFragmentPartition}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.domain.NodeDataNodeParser
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.models.SchemaShape
import amf.validations.ParserSideValidations.InvalidXmlSchemaType
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar, YType}
import amf.core.parser.YMapOps
import amf.core.parser.YNodeLikeOps
import amf.plugins.document.webapi.annotations.ExternalReferenceUrl

case class RamlXmlSchemaExpression(key: YNode,
                                   override val value: YNode,
                                   override val adopt: Shape => Unit,
                                   parseExample: Boolean = false)(override implicit val ctx: RamlWebApiContext)
    extends RamlExternalTypesParser {
  override def parseValue(origin: ValueAndOrigin): SchemaShape = {
    val (maybeReferenceId, maybeLocation, maybeFragmentLabel): (Option[String], Option[String], Option[String]) =
      origin.originalUrlText.map(ReferenceFragmentPartition.apply) match {
        case Some((uriWithoutFragment, fragment)) =>
          val optionalReference = ctx.declarations.fragments.get(uriWithoutFragment)
          val optionalReferenceId = optionalReference.map(
            _.encoded.id + fragment.map(u => if (u.startsWith("/")) u else "/" + u).getOrElse(""))
          val optionalLocation = optionalReference.flatMap(_.location)
          (optionalReferenceId, optionalLocation, fragment)
        case None => (None, None, None)
      }

    val parsed = value.tagType match {
      case YType.Map =>
        val map = value.as[YMap]
        val parsedSchema = nestedTypeOrSchema(map) match {
          case Some(typeEntry: YMapEntry) => buildSchemaShapeFrom(typeEntry)
          case _ =>
            val shape: SchemaShape = emptySchemaShape
            throwInvalidXmlSchemaFormat(shape)
            shape
        }
        parseWrappedFields(map, parsedSchema)
        parsedSchema
      case YType.Seq =>
        val shape: SchemaShape = emptySchemaShape
        throwInvalidXmlSchemaFormat(shape)
        shape
      case _ =>
        val scalar = value.as[YScalar]
        buildSchemaShapeFrom(scalar)
    }
    maybeReferenceId match {
      case Some(r) => parsed.withReference(r)
      case _       => parsed.annotations ++= Annotations(value)
    }
    origin.originalUrlText.foreach(url => parsed.annotations += ExternalReferenceUrl(url))
    parsed.set(SchemaShapeModel.Location, maybeLocation.getOrElse(ctx.loc), Annotations.synthesized())
    maybeFragmentLabel.foreach { parsed.annotations += ExternalFragmentRef(_) }
    parsed
  }

  private def buildSchemaShapeFrom(scalar: YScalar) = {
    val shape = SchemaShape()
      .set(ExternalSourceElementModel.Raw, scalar.text, Annotations(scalar))
      .set(SchemaShapeModel.MediaType, "application/xml", Annotations.synthesized())
    shape.withName(key.as[String])
    adopt(shape)
    shape
  }

  private def parseWrappedFields(map: YMap, parsedSchema: SchemaShape) = {
    map.key("displayName", (ShapeModel.DisplayName in parsedSchema).allowingAnnotations)
    map.key("description", (ShapeModel.Description in parsedSchema).allowingAnnotations)
    map.key(
      "default",
      entry => {
        val dataNodeResult = NodeDataNodeParser(entry.value, parsedSchema.id, quiet = false).parse()
        parsedSchema.setDefaultStrValue(entry)
        dataNodeResult.dataNode.foreach { dataNode =>
          parsedSchema.set(ShapeModel.Default, dataNode, Annotations(entry))
        }
      }
    )
    parseExamples(parsedSchema, value.as[YMap])
  }

  private def buildSchemaShapeFrom(typeEntry: YMapEntry) = {
    val shape = SchemaShape().withRaw(typeEntry.value.toString).withMediaType("application/xml")
    shape.withName(key.as[String])
    adopt(shape)
    shape
  }

  private def throwInvalidXmlSchemaFormat(shape: SchemaShape) = {
    ctx.eh.violation(InvalidXmlSchemaType,
                     shape.id,
                     "Cannot parse XML Schema expression out of a non string value",
                     value)
  }

  private def emptySchemaShape = {
    val shape = SchemaShape()
    adopt(shape)
    shape
  }

  override val externalType: String = "XML"
}
