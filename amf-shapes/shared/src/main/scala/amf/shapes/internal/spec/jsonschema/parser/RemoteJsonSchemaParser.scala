package amf.shapes.internal.spec.jsonschema.parser

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.parser.ShapeParserContext
import amf.shapes.internal.spec.jsonschema.parser.document.JsonSchemaLinker.linkShapeFromDocument
import amf.shapes.internal.spec.jsonschema.ref.JsonSchemaParser

object RemoteJsonSchemaParser {

  def parse(ref: String, fullUrl: String, linkAnnotations: Annotations)(implicit
      ctx: ShapeParserContext
  ): Option[AnyShape] = {
    findReferenceAst(ref) flatMap {
      case (fragment: JsonSchemaDocument, uriFragment) =>
        linkShapeFromDocument(ref, fragment, uriFragment, linkAnnotations)
      case (fragment, uriFragment) =>
        parseShape(fragment, uriFragment)
    } map { shape =>
      updateListeners(shape, fullUrl, ref)
      shape
    }
  }

  private def findReferenceAst(ref: String)(implicit ctx: ShapeParserContext): Option[(BaseUnit, Option[String])] = {
    ctx.getJsonSchemaRefGuide.findJsonReferenceFragment(ref)
  }

  private def parseShape(fragment: BaseUnit, uriFragment: Option[String])(implicit
      ctx: ShapeParserContext
  ): Option[AnyShape] = {
    val newCtx = ctx.copyForBase(fragment)
    new JsonSchemaParser().parse(fragment, uriFragment)(newCtx)
  }

  private def updateListeners(shape: AnyShape, fullUrl: String, ref: String)(implicit ctx: ShapeParserContext): Unit = {
    ctx.registerJsonSchema(fullUrl, shape)
    ctx.futureDeclarations.resolveRef(ref, shape)
  }
}
