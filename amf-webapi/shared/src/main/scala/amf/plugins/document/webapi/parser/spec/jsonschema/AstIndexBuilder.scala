package amf.plugins.document.webapi.parser.spec.jsonschema

import java.net.URI

import amf.core.utils.AliasCounter
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.YMapEntryLike
import amf.plugins.document.webapi.parser.spec.declaration.{
  JSONSchemaDraft201909SchemaVersion,
  JSONSchemaDraft3SchemaVersion,
  JSONSchemaDraft4SchemaVersion,
  JSONSchemaDraft6SchemaVersion,
  JSONSchemaDraft7SchemaVersion,
  JSONSchemaVersion,
  SchemaVersion
}
import amf.validations.ParserSideValidations.ExeededMaxYamlReferences
import org.yaml.model._

object AstIndexBuilder {
  def buildAst(node: YNode, refCounter: AliasCounter, version: SchemaVersion)(implicit ctx: WebApiContext): AstIndex = {
    val locationUri             = getBaseUri(ctx)
    val specificResolutionScope = locationUri.flatMap(loc => getResolutionScope(version, loc)).toSeq
    val scopes                  = Seq(LexicalResolutionScope()) ++ specificResolutionScope
    new AstIndexBuilder(refCounter).build(node, scopes)
  }

  private def getBaseUri(ctx: WebApiContext): Option[URI] =
    try {
      Some(new URI(ctx.jsonSchemaRefGuide.currentLoc))
    } catch {
      case _: Throwable => None
    }

  private def getResolutionScope(version: SchemaVersion, baseUri: URI): Option[ResolutionScope] = version match {
    case JSONSchemaDraft3SchemaVersion | JSONSchemaDraft4SchemaVersion | JSONSchemaDraft6SchemaVersion =>
      Some(Draft4ResolutionScope(baseUri))
    case JSONSchemaDraft7SchemaVersion      => Some(Draft7ResolutionScope(baseUri))
    case JSONSchemaDraft201909SchemaVersion => Some(Draft2019ResolutionScope(baseUri))
    case _                                  => None
  }
}

case class AstIndexBuilder private (private val refCounter: AliasCounter)(implicit ctx: WebApiContext) {

  def build(node: YNode, scopes: Seq[ResolutionScope]): AstIndex = {
    val entrySeq = scopes.flatMap(_.resolve("", YMapEntryLike(node))) ++ index(YMapEntryLike(node), scopes).sortBy(t =>
      t._1)
    AstIndex(entrySeq.toMap)
  }

  private def index(entryLike: YMapEntryLike, scopes: Seq[ResolutionScope]): Seq[(String, YMapEntryLike)] =
    refThresholdExceededOr(entryLike.value) {
      val nextScopes = updateScopes(entryLike, scopes)
      val toReturn = entryLike.value.tagType match {
        case YType.Map => index(entryLike.asMap, nextScopes)
        case YType.Seq => index(entryLike.asSequence, nextScopes)
        case _         => Seq()

      }
      toReturn
    }

  private def updateScopes(entryLike: YMapEntryLike, scopes: Seq[ResolutionScope]): Seq[ResolutionScope] = {
    scopes.map(_.getNext(entryLike))
  }

  private def index(seq: YSequence, scopes: Seq[ResolutionScope]): IndexedSeq[(String, YMapEntryLike)] =
    seq.nodes.zipWithIndex.flatMap {
      case (node, i) => scopes.flatMap(_.resolve(i.toString, YMapEntryLike(node))) ++ index(YMapEntryLike(node), updateScopes(fakeEntryForScopeUpdate(i, node), scopes))
    }

  private def index(map: YMap, scopes: Seq[ResolutionScope]): IndexedSeq[(String, YMapEntryLike)] = {
    map.entries.flatMap { entry =>
      val entries = scopes.flatMap(_.resolve(entry.key.as[YScalar].text, YMapEntryLike(entry)))
      entries ++ index(YMapEntryLike(entry), scopes)
    }
  }

  private def refThresholdExceededOr(node: YNode)(runThunk: => Seq[(String, YMapEntryLike)]) = {
    if (refCounter.exceedsThreshold(node)) {
      ctx.violation(
        ExeededMaxYamlReferences,
        "",
        "Exceeded maximum yaml references threshold"
      )
      Seq()
    } else runThunk
  }

  private def fakeEntryForScopeUpdate(seqIndex: Int, node: YNode): YMapEntryLike = YMapEntryLike(seqIndex.toString, node)
}
