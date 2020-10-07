package amf.plugins.document.webapi.parser.spec.declaration

import amf.client.model.document.Dialect
import amf.core.Root
import amf.core.annotations.Aliases
import amf.core.model.document._
import amf.core.parser.{
  CallbackReferenceCollector,
  CollectionSideEffect,
  ParsedReference,
  Reference,
  ReferenceCollector,
  YMapOps
}
import amf.plugins.document.vocabularies.model.document.Vocabulary
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.features.validation.CoreValidations.ExpectedModule
import amf.validation.DialectValidations.InvalidModuleType
import org.yaml.model.{YMap, YScalar, YType}

/**
  *
  */
case class WebApiRegister()(implicit ctx: WebApiContext) extends CollectionSideEffect[BaseUnit] {
  override def onCollect(alias: String, unit: BaseUnit): Unit = {
    val library = ctx.declarations.getOrCreateLibrary(alias)
    unit match {
      case _: Vocabulary | _: Dialect => ctx.declarations.others += (alias -> unit)
      case d: Module =>
        val library = ctx.declarations.getOrCreateLibrary(alias)
        d.declares.foreach(library += _)
      case fragment: Fragment => ctx.declarations += (alias -> fragment)
      case _                  => // ignore
    }
  }
}

abstract class CommonReferencesParser(references: Seq[ParsedReference])(implicit ctx: WebApiContext) {
  def parse(): ReferenceCollector[BaseUnit] = {
    val result = CallbackReferenceCollector(WebApiRegister())
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

  protected def parseLibraries(declarations: ReferenceCollector[BaseUnit]): Unit
}

case class ReferencesParser(baseUnit: BaseUnit, id: String, key: String, map: YMap, references: Seq[ParsedReference])(
    implicit ctx: WebApiContext)
    extends CommonReferencesParser(references) {

  private def target(url: String): Option[BaseUnit] =
    references.find(r => r.origin.url.equals(url)).map(_.unit)

  override def parseLibraries(result: ReferenceCollector[BaseUnit]): Unit = {
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
  override protected def parseLibraries(declarations: ReferenceCollector[BaseUnit]): Unit = Unit
}

// Helper method to parse references and annotations before having an actual base unit
object ReferencesParserAnnotations {
  def apply(key: String, map: YMap, root: Root)(
      implicit ctx: WebApiContext): (ReferenceCollector[BaseUnit], Option[Aliases]) = {
    val tmp          = Document()
    val declarations = ReferencesParser(tmp, root.location, key, map, root.references).parse()
    (declarations, tmp.annotations.find(classOf[Aliases]))
  }
}
