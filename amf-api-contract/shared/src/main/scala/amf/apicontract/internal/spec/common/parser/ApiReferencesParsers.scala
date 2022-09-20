package amf.apicontract.internal.spec.common.parser

import amf.aml.client.scala.model.document.Dialect
import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.apicontract.internal.spec.raml.parser.document
import amf.apicontract.internal.validation.definitions.ParserSideValidations.InvalidModuleType
import amf.core.client.scala.model.document._
import amf.core.client.scala.parse.document._
import amf.core.internal.annotations.{Aliases, ReferencedInfo}
import amf.core.internal.parser.{Root, YMapOps}
import amf.core.internal.validation.CoreValidations.ExpectedModule
import amf.shapes.client.scala.model.document.JsonSchemaDocument
import amf.shapes.internal.spec.common.parser.{CommonReferencesParser, ReferencesRegister}
import org.mulesoft.common.collections.FilterType
import org.yaml.model.{YMap, YScalar, YType}

/** */
case class WebApiRegister()(implicit ctx: WebApiContext) extends ReferencesRegister {
  override def onCollect(alias: String, unit: BaseUnit): Unit = {
    ctx.declarations.getOrCreateLibrary(alias)
    unit match {
      case d: Module =>
        indexModule(alias, d) { (module, declarations) =>
          module.declares.foreach(declarations += _)
        }
      case fragment: Fragment => ctx.declarations += (alias, fragment)
      case jsonDoc: JsonSchemaDocument =>
        ctx.declarations.documentFragments += (alias -> (jsonDoc.encodes -> buildDeclarationMap(jsonDoc)))
      case _ => // ignore
    }
  }

  protected def indexModule(alias: String, module: Module)(
      indexDeclared: (Module, WebApiDeclarations) => Unit
  ): Unit = {
    val library = ctx.declarations.getOrCreateLibrary(alias)
    indexDeclared(module, library)
    collectExtensions(library, module)
  }

  protected def collectExtensions(library: WebApiDeclarations, l: Module): Unit = {
    l.references
      .filterType[Dialect]
      .foreach { d =>
        library += d.extensionIndex
      }
  }
}

case class WebApiLikeReferencesParser(
    baseUnit: BaseUnit,
    rootLoc: String,
    key: String,
    map: YMap,
    references: Seq[ParsedReference],
    register: ReferencesRegister
)(implicit ctx: WebApiContext)
    extends CommonReferencesParser(references, register) {

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
                val urlOption     = document.LibraryLocationParser(e)(ctx.eh)
                urlOption.foreach { url =>
                  target(url).foreach {
                    case module: DeclaresModel =>
                      module.location().foreach { fullUrl =>
                        collectAlias(baseUnit, alias -> ReferencedInfo(module, fullUrl, url))
                      }
                      result += (alias, module)
                    case other =>
                      ctx.eh.violation(ExpectedModule, rootLoc, s"Expected module but found: $other", e.location)
                  }
                }
              })
          case YType.Null =>
          case _ =>
            ctx.eh.violation(
              InvalidModuleType,
              rootLoc,
              s"Invalid ast type for uses: ${entry.value.tagType}",
              entry.value.location
            )
        }
    )
  }
  private def target(url: String): Option[BaseUnit] = references.find(r => r.origin.url.equals(url)).map(_.unit)

  private def collectAlias(module: BaseUnit, alias: (Aliases.Alias, ReferencedInfo)): BaseUnit = {
    module.annotations.find(classOf[Aliases]) match {
      case Some(aliases) =>
        module.annotations.reject(_.isInstanceOf[Aliases])
        module.add(aliases.copy(aliases = aliases.aliases + alias))
      case None => module.add(Aliases(Set(alias)))
    }
  }
}

object WebApiLikeReferencesParser {

  def apply(baseUnit: BaseUnit, root: Root, key: String)(implicit ctx: WebApiContext): WebApiLikeReferencesParser = {
    val map = root.parsed.asInstanceOf[SyamlParsedDocument].document.as[YMap]
    new WebApiLikeReferencesParser(baseUnit, root.location, key, map, root.references, WebApiRegister())
  }

  def apply(baseUnit: BaseUnit, rootLoc: String, key: String, map: YMap, references: Seq[ParsedReference])(implicit
      ctx: WebApiContext
  ): WebApiLikeReferencesParser = {
    new WebApiLikeReferencesParser(baseUnit, rootLoc, key, map, references, WebApiRegister())
  }
}

case class AsyncReferencesParser(references: Seq[ParsedReference])(implicit ctx: WebApiContext)
    extends CommonReferencesParser(references, WebApiRegister()) {
  override protected def parseLibraries(declarations: ReferenceCollector[BaseUnit]): Unit = Unit
}

// Helper method to parse references and annotations before having an actual base unit
object ReferencesParserAnnotations {
  def apply(key: String, map: YMap, root: Root)(implicit
      ctx: WebApiContext
  ): (ReferenceCollector[BaseUnit], Option[Aliases]) = {
    val tmp          = Document()
    val declarations = WebApiLikeReferencesParser(tmp, root.location, key, map, root.references).parse()
    (declarations, tmp.annotations.find(classOf[Aliases]))
  }
}
