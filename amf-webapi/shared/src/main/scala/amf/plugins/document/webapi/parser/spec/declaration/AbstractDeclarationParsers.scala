package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AbstractVariables, DataNodeParser}
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import org.yaml.model._

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
        e.value.tagType match {
          case YType.Map =>
            e.value
              .as[YMap]
              .entries
              .map(entry =>
                ctx.declarations += AbstractDeclarationParser(producer(entry), customProperties, entry)
                  .parse())
          case YType.Null =>
          case t          => ctx.violation(customProperties, s"Invalid type $t for '$key' node.", e.value)
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
      case Left(link) => parseReferenced(declaration, link, entryValue).adopted(parent)
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
    copied.withId(d.id)
    copied.withName(key)
  }
}
