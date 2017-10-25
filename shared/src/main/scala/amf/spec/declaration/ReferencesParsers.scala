package amf.spec.declaration

import amf.compiler.ParsedReference
import amf.document.{BaseUnit, DeclaresModel, Document}
import amf.document.Fragment.Fragment
import amf.domain.Annotation.Aliases
import amf.domain.dialects.DomainEntity
import amf.spec.Declarations
import org.yaml.model.YMap
import amf.parser.{YMapOps, YValueOps}
import scala.collection.mutable

/**
  *
  */
case class ReferenceDeclarations(references: mutable.Map[String, BaseUnit] = mutable.Map(),
                                 declarations: Declarations = Declarations()) {

  def +=(alias: String, unit: BaseUnit): Unit = {
    references += (alias -> unit)
    val library = declarations.getOrCreateLibrary(alias)
    // todo : ignore domain entities of vocabularies?
    unit match {
      case d: DeclaresModel =>
        d.declares
          .filter({
            case _: DomainEntity => false
            case _               => true
          })
          .foreach(library += _)
    }
  }

  def +=(url: String, fragment: Fragment): Unit = {
    references += (url   -> fragment)
    declarations += (url -> fragment)
  }

  def +=(url: String, fragment: Document): Unit = references += (url -> fragment)

  def solvedReferences(): Seq[BaseUnit] = references.values.toSet.toSeq
}

case class ReferencesParser(key: String, map: YMap, references: Seq[ParsedReference]) {
  def parse(): ReferenceDeclarations = {
    val result: ReferenceDeclarations = parseLibraries()

    references.foreach {
      case ParsedReference(f: Fragment, s: String) => result += (s, f)
      case ParsedReference(d: Document, s: String) => result += (s, d)
      case _                                       =>
    }

    result
  }

  private def target(url: String): Option[BaseUnit] =
    references.find(r => r.parsedUrl.equals(url)).map(_.baseUnit)

  private def parseLibraries(): ReferenceDeclarations = {
    val result = ReferenceDeclarations()

    map.key(
      key,
      entry =>
        entry.value.value.toMap.entries.foreach(e => {
          val alias: String = e.key
          val url: String   = e.value
          target(url).foreach {
            case module: DeclaresModel => result += (alias, addAlias(module, alias)) // this is
            case other =>
              throw new Exception(s"Expected module but found: $other") // todo Uses should only reference modules...
          }
        })
    )

    result
  }

  private def addAlias(module: BaseUnit, alias: String): BaseUnit = {
    val aliasesOption = module.annotations.find(classOf[Aliases])
    if (aliasesOption.isDefined)
      aliasesOption.foreach(a => {
        module.annotations.reject(_.isInstanceOf[Aliases])
        module.add(a.copy(aliases = a.aliases ++ Seq(alias)))
      })
    else
      module.add(Aliases(Seq(alias)))

    module
  }
}
