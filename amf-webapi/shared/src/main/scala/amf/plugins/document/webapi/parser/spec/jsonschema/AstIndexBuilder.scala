package amf.plugins.document.webapi.parser.spec.jsonschema

import java.net.URI
import amf.core.utils.AliasCounter
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft201909SchemaVersion,
  JSONSchemaDraft3SchemaVersion,
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaDraft6SchemaVersion,
  JSONSchemaDraft7SchemaVersion,
  JSONSchemaVersion,
  SchemaVersion
}
import amf.validations.ShapeParserSideValidations.ExceededMaxYamlReferences
import org.yaml.model._

import scala.collection.mutable

object AstIndexBuilder {
  def buildAst(node: YNode, refCounter: AliasCounter, version: SchemaVersion)(implicit ctx: WebApiContext): AstIndex = {
    val locationUri             = getBaseUri(ctx)
    val specificResolutionScope = locationUri.flatMap(loc => getResolutionScope(version, loc)).toSeq
    val scopes                  = Seq(LexicalResolutionScope()) ++ specificResolutionScope
    val resolvers               = Seq(FragmentTraversingResolver)
    new AstIndexBuilder(refCounter).build(node, scopes, resolvers)
  }

  private def getBaseUri(ctx: WebApiContext): Option[URI] =
    try {
      Some(new URI(ctx.jsonSchemaRefGuide.currentLoc))
    } catch {
      case _: Throwable => None
    }

  private def getResolutionScope(version: SchemaVersion, baseUri: URI): Option[ResolutionScope] = version match {
    case JSONSchemaDraft3SchemaVersion | JSONSchemaDraft4SchemaVersion | JSONSchemaDraft6SchemaVersion =>
      Some(Draft4IdResolutionScope(baseUri))
    case JSONSchemaDraft7SchemaVersion      => Some(Draft7IdResolutionScope(baseUri))
    case JSONSchemaDraft201909SchemaVersion => Some(Draft2019ResolutionScope(baseUri))
    case _                                  => None
  }
}

case class AstIndexBuilder private (private val refCounter: AliasCounter)(implicit ctx: WebApiContext) {

  def build(node: YNode, scopes: Seq[ResolutionScope], resolvers: Seq[ReferenceResolver]): AstIndex = {
    val acc = mutable.Map.empty[String, YMapEntryLike]
    scopes.foreach(_.resolve("", YMapEntryLike(node), acc))
    index(YMapEntryLike(node), scopes, acc)
    AstIndex(acc, resolvers)
  }

  private def index(entryLike: YMapEntryLike,
                    scopes: Seq[ResolutionScope],
                    acc: mutable.Map[String, YMapEntryLike]): Unit =
    refThresholdExceededOr(entryLike.value) {
      entryLike.value.tagType match {
        case YType.Map =>
          val nextScopes = updateScopes(entryLike, scopes)
          index(entryLike.asMap, nextScopes, acc)
        case YType.Seq =>
          val nextScopes = updateScopes(entryLike, scopes)
          index(entryLike.asSequence, nextScopes, acc)
        case _ => Seq()

      }
    }

  private def updateScopes(entryLike: YMapEntryLike, scopes: Seq[ResolutionScope]): Seq[ResolutionScope] = {
    scopes.map(_.getNext(entryLike))
  }

  private def index(seq: YSequence, scopes: Seq[ResolutionScope], acc: mutable.Map[String, YMapEntryLike]): Unit =
    seq.nodes.zipWithIndex.foreach {
      case (node, i) =>
        scopes.foreach(_.resolve(i.toString, YMapEntryLike(node), acc))
        index(YMapEntryLike(node), updateScopes(fakeEntryForScopeUpdate(i, node), scopes), acc)
    }

  private def index(map: YMap, scopes: Seq[ResolutionScope], acc: mutable.Map[String, YMapEntryLike]): Unit = {
    map.entries.foreach { entry =>
      entry.key.asOption[YScalar].foreach { scalarKey =>
        scopes.foreach(_.resolve(scalarKey.text, YMapEntryLike(entry), acc))
      }
      index(YMapEntryLike(entry), scopes, acc)
    }
  }

  private def refThresholdExceededOr(node: YNode)(runThunk: => Unit): Unit = {
    if (refCounter.exceedsThreshold(node)) {
      ctx.violation(
        ExceededMaxYamlReferences,
        "",
        "Exceeded maximum yaml references threshold"
      )
    } else runThunk
  }

  private def fakeEntryForScopeUpdate(seqIndex: Int, node: YNode): YMapEntryLike =
    YMapEntryLike(seqIndex.toString, node)
}
