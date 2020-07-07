package amf.plugins.document.webapi.parser.spec.declaration.external.raml

import amf.core.annotations.{ExternalFragmentRef, LexicalInformation}
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, Range, ReferenceFragmentPartition}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.spec.domain.NodeDataNodeParser
import amf.plugins.domain.shapes.metamodel.SchemaShapeModel
import amf.plugins.domain.shapes.models.SchemaShape
import amf.validations.ParserSideValidations.InvalidXmlSchemaType
import org.yaml.model.{YMap, YMapEntry, YNode, YPart, YScalar, YType}
import amf.core.parser.YMapOps
import amf.core.parser.YNodeLikeOps

case class RamlXmlSchemaExpression(key: YNode,
                                   override val value: YNode,
                                   override val adopt: Shape => Unit,
                                   parseExample: Boolean = false,
                                   parentNode: Option[YPart] = None)(override implicit val ctx: RamlWebApiContext)
    extends RamlExternalTypesParser {
  override def parseValue(origin: ValueAndOrigin): SchemaShape = {
    val (maybeReferenceId, maybeLocation, maybeFragmentLabel): (Option[String], Option[String], Option[String]) =
      origin.oriUrl
        .map(ReferenceFragmentPartition.apply) match {
        case Some((path, uri)) =>
          val maybeRef = ctx.declarations.fragments
            .get(path)
          (maybeRef
             .map(_.encoded.id + uri.map(u => if (u.startsWith("/")) u else "/" + u).getOrElse("")),
           maybeRef.flatMap(_.location),
           uri)
        case None => (None, None, None)
      }

    val parsed = value.tagType match {
      case YType.Map =>
        val map = value.as[YMap]
        val parsedSchema = nestedTypeOrSchema(map) match {
          case Some(typeEntry: YMapEntry) if typeEntry.value.toOption[YScalar].isDefined =>
            val shape =
              SchemaShape().withRaw(typeEntry.value.as[YScalar].text).withMediaType("application/xml")
            shape.withName(key.as[String])
            adopt(shape)
            shape
          case _ =>
            val shape = SchemaShape()
            adopt(shape)
            ctx.eh.violation(InvalidXmlSchemaType,
                             shape.id,
                             "Cannot parse XML Schema expression out of a non string value",
                             value)
            shape
        }
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

        parsedSchema
      case YType.Seq =>
        val shape = SchemaShape()
        adopt(shape)
        ctx.eh.violation(InvalidXmlSchemaType,
                         shape.id,
                         "Cannot parse XML Schema expression out of a non string value",
                         value)
        shape
      case _ =>
        val shape = parentNode.map(SchemaShape(_)).getOrElse(SchemaShape())
        val raw   = value.as[YScalar].text
        shape.withRaw(raw).withMediaType("application/xml")
        shape.withName(key.as[String])
        adopt(shape)
        shape
    }
    maybeReferenceId match {
      case Some(r) => parsed.withReference(r)
      case _       => parsed.annotations += LexicalInformation(Range(value.range))
    }
    parsed.set(SchemaShapeModel.Location, maybeLocation.getOrElse(ctx.loc))
    maybeFragmentLabel.foreach { parsed.annotations += ExternalFragmentRef(_) }
    parsed
  }

  override val externalType: String = "XML"
}
