package amf.spec.declaration

import amf.domain.Annotations
import amf.domain.`abstract`.{AbstractDeclaration, ResourceType, Trait}
import amf.parser.YMapOps
import amf.parser.{YMapOps, YNodeLikeOps}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.spec.{ParserContext, SearchScope}
import amf.spec.common.{AbstractVariables, DataNodeParser}
import amf.spec.{ParserContext, SearchScope}
import org.yaml.model.{YMap, YMapEntry, YNode, YPart}

/**
  *
  */
case class AbstractDeclarationsParser(key: String,
                                      producer: (YMapEntry) => AbstractDeclaration,
                                      map: YMap,
                                      customProperties: String)(implicit ctx: WebApiContext) {
  def parse(): Unit = {
    map.key(
      key,
      e => {
        e.value.to[YMap] match {
          case Right(declarations) =>
            declarations.entries
              .map(
                entry =>
                  ctx.declarations += AbstractDeclarationParser(producer(entry), customProperties, entry)
                    .parse())
          case Left(_) =>
        }
      }
    )
  }
}

object AbstractDeclarationParser {

  def apply(declaration: AbstractDeclaration, parent: String, entry: YMapEntry)(
      implicit ctx: WebApiContext): AbstractDeclarationParser =
    new AbstractDeclarationParser(declaration, parent, entry.key, entry.value)
}

case class AbstractDeclarationParser(declaration: AbstractDeclaration, parent: String, key: String, entryValue: YNode)(
    implicit ctx: WebApiContext) {
  def parse(): AbstractDeclaration = {

    ctx.link(entryValue) match {
      case Left(link) => parseReferenced(declaration, link, entryValue)
      case Right(value) =>
        val variables = AbstractVariables()
        val dataNode  = DataNodeParser(value, variables, Some(parent + s"/$key")).parse()

        declaration.withName(key).adopted(parent).withDataNode(dataNode)

        variables.ifNonEmpty(p => declaration.withVariables(p))

        declaration
    }
  }

  def parseReferenced(declared: AbstractDeclaration, parsedUrl: String, ast: YPart): AbstractDeclaration = {
    val d: AbstractDeclaration = declared match {
      case _: Trait        => ctx.declarations.findTraitOrError(ast)(parsedUrl, SearchScope.Fragments)
      case _: ResourceType => ctx.declarations.findResourceTypeOrError(ast)(parsedUrl, SearchScope.Fragments)
    }
    val copied: AbstractDeclaration = d.link(parsedUrl, Annotations(ast))
    copied.withName(key)
  }
}
