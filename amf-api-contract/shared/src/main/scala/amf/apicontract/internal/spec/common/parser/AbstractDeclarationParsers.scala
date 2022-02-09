package amf.apicontract.internal.spec.common.parser

import amf.aml.internal.parse.common.{DeclarationKey, DeclarationKeyCollector}
import amf.core.client.scala.model.domain.templates.AbstractDeclaration
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.annotations.{DeclaredElement, ExternalFragmentRef}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.metamodel.domain.templates.AbstractDeclarationModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode, SearchScope}
import amf.apicontract.client.scala.model.domain.templates.{ResourceType, Trait}
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  InvalidAbstractDeclarationType,
  NullAbstractDeclaration
}
import amf.core.internal.datanode.DataNodeParser
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import amf.shapes.internal.spec.datanode.AbstractVariables
import org.yaml.model._

/**
  *
  */
case class AbstractDeclarationsParser(key: String,
                                      producer: YMapEntry => AbstractDeclaration,
                                      map: YMap,
                                      customProperties: String,
                                      model: DomainElementModel,
                                      declarationKeyCollector: DeclarationKeyCollector)(implicit ctx: WebApiContext) {
  def parse(): Unit = {
    map.key(
      key,
      e => {
        declarationKeyCollector.addDeclarationKey(DeclarationKey(e))
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
                             e.value.location)
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

  def parse(): AbstractDeclaration = {

    val entryValue: YNode = map.value

    if (entryValue.tagType == YType.Null)
      ctx.eh.warning(NullAbstractDeclaration,
                     parent,
                     "Generating abstract declaration (resource type / trait)  with null value",
                     entryValue.location)

    ctx.link(entryValue) match {
      case Left(link) => parseReferenced(declaration, link, entryValue, map.annotations)
      case Right(value) =>
        val variables = AbstractVariables()(ctx)
        named(declaration)
        val filteredNode: YNode = value.tagType match {
          case YType.Map =>
            value
              .as[YMap]
              .key("usage", { entry =>
                val usage = ScalarNode(entry.value)
                declaration.setWithoutId(AbstractDeclarationModel.Description, usage.string(), Annotations(entry))
              })
            val fields = value.as[YMap].entries.filter(_.key.as[YScalar].text != "usage")
            YMap(fields, fields.headOption.map(_.sourceName).getOrElse(""))
          case _ =>
            value
        }
        val dataNode =
          DataNodeParser(filteredNode, variables)(WebApiShapeParserContextAdapter(ctx)).parse()
        declaration.setWithoutId(AbstractDeclarationModel.DataNode, dataNode, Annotations(filteredNode))

        variables.ifNonEmpty(
          p =>
            declaration
              .setWithoutId(AbstractDeclarationModel.Variables,
                            AmfArray(p, Annotations(value.value)),
                            Annotations(value)))

        declaration
    }
  }

  private def named(declaration: AbstractDeclaration): Unit = {
    map.key.foreach { key =>
      val element = ScalarNode(key).text()
      declaration.setWithoutId(AbstractDeclarationModel.Name, element, element.annotations)
    }
  }

  def parseReferenced(declared: AbstractDeclaration,
                      parsedUrl: String,
                      ast: YPart,
                      elementAnn: Annotations): AbstractDeclaration = {
    val d: AbstractDeclaration = declared match {
      case _: Trait        => ctx.declarations.findTraitOrError(ast)(parsedUrl, SearchScope.Fragments)
      case _: ResourceType => ctx.declarations.findResourceTypeOrError(ast)(parsedUrl, SearchScope.Fragments)
    }
    val copied: AbstractDeclaration = d.link(AmfScalar(parsedUrl), elementAnn, Annotations.synthesized())
    copied.add(ExternalFragmentRef(parsedUrl))
    copied.withId(d.id)
    named(copied)
    copied
  }
}
