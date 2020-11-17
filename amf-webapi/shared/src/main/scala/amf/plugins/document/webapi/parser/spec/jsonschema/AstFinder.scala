package amf.plugins.document.webapi.parser.spec.jsonschema

import amf.core.Root
import amf.core.model.document.Fragment
import amf.core.parser.SyamlParsedDocument
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.jsonschema.JsonSchemaRootCreator.createRootFrom
import amf.validations.ParserSideValidations.UnableToParseJsonSchema
import org.yaml.model.YNode

object AstFinder {

  def findAst(inputFragment: Fragment, pointer: Option[String])(implicit ctx: WebApiContext): Option[YNode] = {
    val doc = createRootFrom(inputFragment, pointer, ctx.eh)
    findAst(doc, ctx)
  }

  private def findAst(doc: Root, context: WebApiContext): Option[YNode] = {
    doc.parsed match {
      case parsedDoc: SyamlParsedDocument =>
        val shapeId: String                  = if (doc.location.contains("#")) doc.location else doc.location + "#/"
        val JsonReference(url, hashFragment) = JsonReference.buildReference(doc.location)
        val rootAst                          = getPointedAstOrNode(parsedDoc.document.node, shapeId, hashFragment, url, context)
        Some(rootAst.value)
      case _ => None
    }
  }

  // TODO: having shapeId and url params here is quite ugly. They are only used for the error. It smells.
  def getPointedAstOrNode(node: YNode,
                          shapeId: String,
                          hashFragment: Option[String],
                          url: String,
                          ctx: WebApiContext): YMapEntryLike = {
    ctx.setJsonSchemaAST(node)
    val rootAst = hashFragment match {
      case Some(fragment) => findNodeInIndex(fragment, ctx)
      case None => Some(YMapEntryLike(node))
    }
    rootAst.getOrElse {
      ctx.eh.violation(UnableToParseJsonSchema,
                       shapeId,
                       s"Cannot find path ${hashFragment.getOrElse("")} in JSON schema $url",
                       node)
      YMapEntryLike(node)
    }
  }

  // TODO: maybe we should decouple the JsonSchemaIndex from the ctx. Just a thought as it doesn't make sense t pass a ctx to a findNodeInIndex method.
  private def findNodeInIndex(path: String, ctx: WebApiContext): Option[YMapEntryLike] = ctx.findLocalJSONPath(path)
}


