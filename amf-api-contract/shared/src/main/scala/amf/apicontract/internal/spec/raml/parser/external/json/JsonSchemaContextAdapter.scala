package amf.apicontract.internal.spec.raml.parser.external.json

import amf.apicontract.internal.spec.common.parser.WebApiContext
import amf.apicontract.internal.spec.jsonschema.JsonSchemaWebApiContext
import amf.apicontract.internal.spec.spec.toJsonSchema
import amf.core.client.scala.parse.document.{ParsedReference, Reference}
import org.yaml.model.YNode.MutRef
import org.yaml.model.{YNode, YScalar, YType}

object JsonSchemaContextAdapter {

  def toSchemaContext(ctx: WebApiContext, ast: YNode): JsonSchemaWebApiContext = {
    ast match {
      case inlined: MutRef =>
        if (isExternalFile(inlined)) {
          // JSON schema file we need to update the context
          val rawPath            = inlined.origValue.asInstanceOf[YScalar].text
          val normalizedFilePath = stripPointsAndFragment(rawPath)
          ctx.refs.find(r => r.unit.location().exists(_.endsWith(normalizedFilePath))) match {
            case Some(ref) =>
              toJsonSchema(
                ref.unit.location().get,
                ref.unit.references.map(r => ParsedReference(r, Reference(ref.unit.location().get, Nil), None)),
                ctx
              )
            case _
                if hasLocation(
                  ast
                ) => // external fragment from external fragment case. The target value ast has the real source name of the faile. (There is no external fragment because was inlined)
              toJsonSchema(ast.value.sourceName, ctx.refs, ctx)
            case _ => toJsonSchema(ctx)
          }
        } else {
          // Inlined we don't need to update the context for ths JSON schema file
          toJsonSchema(ctx)
        }
      case _ =>
        toJsonSchema(ctx)
    }
  }

  private def hasLocation(ast: YNode) = {
    Option(
      ast.value.sourceName
    ).isDefined
  }

  private def isExternalFile(inlined: MutRef) = inlined.origTag.tagType == YType.Include

  private def stripPointsAndFragment(rawPath: String): String = {
    //    TODO: we need to resolve paths but this conflicts with absolute references to exchange_modules
    //    val file = rawPath.split("#").head
    //    val root               = ctx.rootContextDocument
    //    val normalizedFilePath = ctx.resolvedPath(root, file)
    val hashTagIdx = rawPath.indexOf("#")
    val parentIdx  = rawPath.lastIndexOf("../") + 3
    val currentIdx = rawPath.lastIndexOf("./") + 2
    val start      = parentIdx.max(currentIdx).max(0)
    val finish     = if (hashTagIdx == -1) rawPath.length else hashTagIdx
    rawPath.substring(start, finish)
  }
}
