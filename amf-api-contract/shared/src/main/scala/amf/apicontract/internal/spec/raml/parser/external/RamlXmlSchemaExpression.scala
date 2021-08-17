package amf.apicontract.internal.spec.raml.parser.external

import amf.apicontract.internal.spec.common.parser.WebApiShapeParserContextAdapter
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.InvalidXmlSchemaType
import amf.core.client.scala.model.domain.{AmfScalar, Shape}
import amf.core.client.scala.parse.document.ReferenceFragmentPartition
import amf.core.internal.annotations.ExternalFragmentRef
import amf.core.internal.metamodel.domain.{ExternalSourceElementModel, ShapeModel}
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Mimes
import amf.core.internal.remote.Mimes._
import amf.shapes.internal.annotations.ExternalReferenceUrl
import amf.shapes.client.scala.model.domain.SchemaShape
import amf.shapes.internal.domain.metamodel.SchemaShapeModel
import amf.shapes.internal.spec.common.parser.NodeDataNodeParser
import amf.shapes.internal.spec.raml.parser.external.RamlExternalTypesParser
import amf.shapes.internal.spec.{RamlExternalSchemaExpressionFactory, ShapeParserContext}
import org.yaml.model._

case class DefaultRamlExternalSchemaExpressionFactory()(implicit val ctx: RamlWebApiContext)
    extends RamlExternalSchemaExpressionFactory {
  def createXml(key: YNode, value: YNode, adopt: Shape => Unit, parseExample: Boolean = false) =
    RamlXmlSchemaExpression(key, value, adopt, parseExample)
  def createJson(key: YNode, value: YNode, adopt: Shape => Unit, parseExample: Boolean = false) =
    RamlJsonSchemaExpression(key, value, adopt, parseExample)
}

case class RamlXmlSchemaExpression(key: YNode,
                                   override val value: YNode,
                                   override val adopt: Shape => Unit,
                                   parseExample: Boolean = false)(implicit val ctx: RamlWebApiContext)
    extends RamlExternalTypesParser {

  override val shapeCtx: ShapeParserContext = WebApiShapeParserContextAdapter(ctx)

  override def parseValue(origin: ValueAndOrigin): SchemaShape = {
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

    val (maybeLocation, maybeFragmentLabel): (Option[String], Option[String]) =
      origin.originalUrlText.map(ReferenceFragmentPartition.apply) match {
        case Some((uriWithoutFragment, fragment)) =>
          val optionalReference = ctx.declarations.fragments.get(uriWithoutFragment)
          optionalReference match {
            case Some(ref) =>
              parsed.callAfterAdoption{() =>
                val refId = ref.encoded.id + fragment.map(u => if (u.startsWith("/")) u else "/" + u).getOrElse("")
                parsed.withReference(refId)
              }
            case None => parsed.annotations ++= Annotations(value)
          }
          val optionalLocation = optionalReference.flatMap(_.location)
          (optionalLocation, fragment)
        case None =>
          parsed.annotations ++= Annotations(value)
          (None, None)
      }

    origin.originalUrlText.foreach(url => parsed.annotations += ExternalReferenceUrl(url))
    parsed.set(SchemaShapeModel.Location, maybeLocation.getOrElse(ctx.loc), Annotations.synthesized())
    maybeFragmentLabel.foreach { parsed.annotations += ExternalFragmentRef(_) }
    parsed
  }

  private def buildSchemaShapeFrom(scalar: YScalar) = {
    val shape = SchemaShape()
      .set(ExternalSourceElementModel.Raw, AmfScalar(scalar.text, Annotations(scalar)), Annotations.inferred())
      .set(SchemaShapeModel.MediaType, `application/xml`, Annotations.synthesized())
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
        val dataNodeResult =
          NodeDataNodeParser(entry.value, parsedSchema.id, quiet = false)(WebApiShapeParserContextAdapter(ctx)).parse()
        parsedSchema.setDefaultStrValue(entry)
        dataNodeResult.dataNode.foreach { dataNode =>
          parsedSchema.set(ShapeModel.Default, dataNode, Annotations(entry))
        }
      }
    )
    parseExamples(parsedSchema, value.as[YMap])(shapeCtx)
  }

  private def buildSchemaShapeFrom(typeEntry: YMapEntry) = {
    val shape = SchemaShape()
    shape
      .set(ExternalSourceElementModel.Raw,
           AmfScalar(typeEntry.value.toString, Annotations(typeEntry.value)),
           Annotations.inferred())
      .set(SchemaShapeModel.MediaType, `application/xml`, Annotations.synthesized())
    shape.withName(key.as[String], Annotations(key))
    adopt(shape)
    shape
  }

  private def throwInvalidXmlSchemaFormat(shape: SchemaShape) = {
    ctx.eh.violation(InvalidXmlSchemaType,
                     shape,
                     "Cannot parse XML Schema expression out of a non string value",
                     value.location)
  }

  private def emptySchemaShape = {
    val shape = SchemaShape()
    adopt(shape)
    shape
  }

  override val externalType: String = "XML"
}
