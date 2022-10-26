package amf.shapes.internal.spec.jsonschema.ref

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.SyamlParsedDocument
import amf.core.internal.parser.Root
import amf.shapes.internal.spec.common.parser.{ShapeParserContext, YMapEntryLike}
import amf.shapes.internal.spec.jsonschema.ref.JsonSchemaRootCreator.createRootFrom
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.UnableToParseJsonSchema
import org.yaml.model.YNode

object AstFinder {

  def findAst(inputFragment: BaseUnit, pointer: Option[String])(implicit ctx: ShapeParserContext): Option[YNode] = {
    val doc = createRootFrom(inputFragment, pointer, ctx)
    findAst(doc, ctx)
  }

  private def findAst(doc: Root, context: ShapeParserContext): Option[YNode] = {
    doc.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String                  = if (doc.location.contains("#")) doc.location else doc.location + "#/"
        val JsonReference(url, hashFragment) = JsonReference.buildReference(doc.location)
        val rootAst = getPointedAstOrNode(parsedDoc.document.node, shapeId, hashFragment, url, context)
        Some(rootAst.value)
      case _ => None
    }
  }

  // TODO: having shapeId and url params here is quite ugly. They are only used for the error. It smells.
  def getPointedAstOrNode(
      node: YNode,
      shapeId: String,
      hashFragment: Option[String],
      url: String,
      ctx: ShapeParserContext
  ): YMapEntryLike = {

    implicit val errorHandler: AMFErrorHandler = ctx.eh

    ctx.setJsonSchemaAST(node)
    val rootAst = hashFragment match {
      case Some(fragment) => findNodeInIndex(fragment, ctx)
      case None           => Some(YMapEntryLike(node)(ctx))
    }
    rootAst.getOrElse {
      ctx.eh.violation(
        UnableToParseJsonSchema,
        shapeId,
        s"Cannot find path ${hashFragment.getOrElse("")} in JSON schema $url",
        node.location
      )
      YMapEntryLike(node)
    }
  }

  // TODO: maybe we should decouple the JsonSchemaIndex from the ctx. Just a thought as it doesn't make sense t pass a ctx to a findNodeInIndex method.
  private def findNodeInIndex(path: String, ctx: ShapeParserContext): Option[YMapEntryLike] =
    ctx.findLocalJSONPath(path)
}
