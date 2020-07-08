package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.DeclaredElement
import amf.core.metamodel.domain.templates.AbstractDeclarationModel
import amf.core.model.domain.AmfScalar
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AbstractVariables, DataNodeParser, YMapEntryLike}
import amf.plugins.domain.webapi.models.templates.{ResourceType, Trait}
import amf.validations.ParserSideValidations.{InvalidAbstractDeclarationType, NullAbstractDeclaration}
import org.yaml.model._

/**
  *
  */
case class AbstractDeclarationsParser(key: String,
                                      producer: YMapEntry => AbstractDeclaration,
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
              .map(
                entry =>
                  ctx.declarations += AbstractDeclarationParser(producer(entry), customProperties, entry)
                    .parse()
                    .add(DeclaredElement()))
          case YType.Null =>
          case t =>
            ctx.eh.violation(InvalidAbstractDeclarationType,
                             customProperties,
                             s"Invalid type $t for '$key' node.",
                             e.value)
        }
      }
    )
  }
}

object AbstractDeclarationParser {

  def apply(declaration: AbstractDeclaration, parent: String, entry: YMapEntry)(
      implicit ctx: WebApiContext): AbstractDeclarationParser =
    new AbstractDeclarationParser(declaration, parent, YMapEntryLike(entry))
}

case class AbstractDeclarationParser(declaration: AbstractDeclaration, parent: String, map: YMapEntryLike)(
    implicit ctx: WebApiContext) {
  val key: String = map.key
    .map(_.as[YScalar].text)
    .getOrElse(declaration match {
      case _: Trait        => "trait"
      case _: ResourceType => "resourceType"
      case _               => "abstractDeclaration"
    })
  val annotations: Annotations = map.key.map(k => Annotations(k)).getOrElse(Annotations())

  def parse(): AbstractDeclaration = {

    val entryValue: YNode = map.value

    if (entryValue.tagType == YType.Null)
      ctx.eh.warning(NullAbstractDeclaration,
                     parent,
                     "Generating abstract declaration (resource type / trait)  with null value",
                     entryValue)

    ctx.link(entryValue) match {
      case Left(link) => parseReferenced(declaration, link, entryValue).adopted(parent)
      case Right(value) =>
        val variables = AbstractVariables()
        val parentUri =
          if (parent.contains("#")) s"$parent/$key"
          else s"$parent#/$key"
        val filteredNode: YNode = value.tagType match {
          case YType.Map =>
            value
              .as[YMap]
              .key("usage", { usage =>
                declaration.set(AbstractDeclarationModel.Description,
                                AmfScalar(usage.value.as[String], Annotations(usage)))
              })
            val fields = value.as[YMap].entries.filter(_.key.as[YScalar].text != "usage")
            YMap(fields, fields.headOption.map(_.sourceName).getOrElse(""))
          case _ =>
            value
        }
        val dataNode = DataNodeParser(filteredNode, variables, Some(parentUri)).parse()

        declaration.withName(key, annotations).adopted(parent).withDataNode(dataNode)

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
    copied.withName(key, annotations)
  }
}
