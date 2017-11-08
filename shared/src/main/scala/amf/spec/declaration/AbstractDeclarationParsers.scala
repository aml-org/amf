package amf.spec.declaration

import amf.domain.Annotations
import amf.domain.`abstract`.{AbstractDeclaration, ResourceType, Trait}
import amf.spec.Declarations
import amf.spec.common.{AbstractVariables, DataNodeParser, SpecParserContext}
import org.yaml.model.{YMap, YMapEntry, YNode}
import amf.parser.{YMapOps, YValueOps}

/**
  *
  */
case class AbstractDeclarationsParser(key: String,
                                      producer: (YMapEntry) => AbstractDeclaration,
                                      map: YMap,
                                      customProperties: String,
                                      declarations: Declarations)(implicit spec: SpecParserContext) {
  def parse(): Unit = {
    map.key(
      key,
      e => {
        e.value.value.toMap.entries.map(traitEntry =>
          declarations += AbstractDeclarationParser(producer(traitEntry), customProperties, traitEntry, declarations)
            .parse())
      }
    )
  }
}

object AbstractDeclarationParser {

  def apply(declaration: AbstractDeclaration, parent: String, entry: YMapEntry, declarations: Declarations)(
      implicit spec: SpecParserContext): AbstractDeclarationParser =
    new AbstractDeclarationParser(declaration, parent, entry.key, entry.value, declarations)
}

case class AbstractDeclarationParser(declaration: AbstractDeclaration,
                                     parent: String,
                                     key: String,
                                     entryValue: YNode,
                                     declarations: Declarations)(implicit spec: SpecParserContext) {
  def parse(): AbstractDeclaration = {

    spec.link(entryValue) match {
      case Left(link) => parseReferenced(declaration, link, Annotations(entryValue))
      case Right(value) =>
        val variables = AbstractVariables()
        val dataNode  = DataNodeParser(value, variables, Some(parent + s"/$key")).parse()

        declaration.withName(key).adopted(parent).withDataNode(dataNode)

        variables.ifNonEmpty(p => declaration.withVariables(p))

        declaration
    }
  }

  def parseReferenced(declared: AbstractDeclaration,
                      parsedUrl: String,
                      annotations: Annotations): AbstractDeclaration = {
    val d = declared match {
      case _: Trait        => declarations.findTrait(parsedUrl)
      case _: ResourceType => declarations.findResourceType(parsedUrl)
    }
    d.map { a =>
        val copied: AbstractDeclaration = a.link(parsedUrl, annotations)
        copied.withName(key)
      }
      .getOrElse(throw new IllegalStateException("Could not find abstract declaration in references map for link"))
  }
}
