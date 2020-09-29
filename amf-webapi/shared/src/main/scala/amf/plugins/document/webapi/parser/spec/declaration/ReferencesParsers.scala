package amf.plugins.document.webapi.parser.spec.declaration

import amf.client.model.document.Dialect
import amf.core.Root
import amf.core.annotations.Aliases
import amf.core.model.document.{BaseUnit, DeclaresModel, Document, Fragment, Module}
import amf.core.parser.{ParsedReference, _}
import amf.plugins.document.vocabularies.model.document.Vocabulary
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.validation.DialectValidations.InvalidModuleType
import amf.plugins.features.validation.CoreValidations.ExpectedModule
import org.yaml.model.{YMap, YMapEntry, YScalar, YType}

import scala.collection.mutable

/**
  *
  */
object ReferenceDeclarations {
  def apply(references: mutable.Map[String, BaseUnit])(implicit ctx: WebApiContext) =
    new ReferenceDeclarations(references)

  def apply()(implicit ctx: WebApiContext): ReferenceDeclarations = apply(mutable.Map[String, BaseUnit]())
}

case class ReferenceDeclarations(references: mutable.Map[String, BaseUnit] = mutable.Map())(
    implicit ctx: WebApiContext) {

  def +=(alias: String, unit: BaseUnit): Unit = {
    references += (alias -> unit)
    val library = ctx.declarations.getOrCreateLibrary(alias)
    unit match {
      case _ @(_: Vocabulary | _: Dialect) => ctx.declarations.others += (alias -> unit)
      case d: DeclaresModel                => d.declares.foreach(library += _)
    }
  }

  def +=(url: String, fragment: Fragment): Unit = {
    references += (url       -> fragment)
    ctx.declarations += (url -> fragment)
  }

  def +=(url: String, fragment: Document): Unit = references += (url -> fragment)

  def solvedReferences(): Seq[BaseUnit] = references.values.toSet.toSeq
}

abstract class CommonReferencesParser(references: Seq[ParsedReference])(implicit ctx: WebApiContext) {
  def parse(): ReferenceDeclarations = {
    val result = ReferenceDeclarations()
    parseLibraries(result)
    references.foreach {
      case ParsedReference(f: Fragment, origin: Reference, _)                          => result += (origin.url, f)
      case ParsedReference(d: Document, origin: Reference, _)                          => result += (origin.url, d)
      case ParsedReference(m: Module, origin: Reference, _)                            => result += (origin.url, m)
      case ParsedReference(other @ (_: Vocabulary | _: Dialect), origin: Reference, _) => result += (origin.url, other)
      case _                                                                           => // Nothing
    }
    result
  }

  protected def parseLibraries(declarations: ReferenceDeclarations): Unit
}

case class ReferencesParser(baseUnit: BaseUnit, id: String, key: String, map: YMap, references: Seq[ParsedReference])(
    implicit ctx: WebApiContext)
    extends CommonReferencesParser(references) {

  private def target(url: String): Option[BaseUnit] =
    references.find(r => r.origin.url.equals(url)).map(_.unit)

  override def parseLibraries(result: ReferenceDeclarations): Unit = {
    map.key(
      key,
      entry =>
        entry.value.tagType match {
          case YType.Map =>
            entry.value
              .as[YMap]
              .entries
              .foreach(e => {
                val alias: String = e.key.as[YScalar].text
                val urlOption     = LibraryLocationParser(e, ctx)
                urlOption.foreach { url =>
                  target(url).foreach {
                    case module: DeclaresModel =>
                      collectAlias(baseUnit, alias -> (module.id, url))
                      result += (alias, module)
                    case other =>
                      ctx.eh.violation(ExpectedModule, id, s"Expected module but found: $other", e)
                  }
                }
              })
          case YType.Null =>
          case _ =>
            ctx.eh.violation(InvalidModuleType, id, s"Invalid ast type for uses: ${entry.value.tagType}", entry.value)
      }
    )
  }

  private def collectAlias(module: BaseUnit,
                           alias: (Aliases.Alias, (Aliases.FullUrl, Aliases.RelativeUrl))): BaseUnit = {
    module.annotations.find(classOf[Aliases]) match {
      case Some(aliases) =>
        module.annotations.reject(_.isInstanceOf[Aliases])
        module.add(aliases.copy(aliases = aliases.aliases + alias))
      case None => module.add(Aliases(Set(alias)))
    }
  }
}

case class AsycnReferencesParser(references: Seq[ParsedReference])(implicit ctx: WebApiContext)
    extends CommonReferencesParser(references) {
  override protected def parseLibraries(declarations: ReferenceDeclarations): Unit = Unit
}

// Helper method to parse references and annotations before having an actual base unit
object ReferencesParserAnnotations {
  def apply(key: String, map: YMap, root: Root)(
      implicit ctx: WebApiContext): (ReferenceDeclarations, Option[Aliases]) = {
    val tmp          = Document()
    val declarations = ReferencesParser(tmp, root.location, key, map, root.references).parse()
    (declarations, tmp.annotations.find(classOf[Aliases]))
  }
}
