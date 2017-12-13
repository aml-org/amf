package amf.plugins.document.vocabularies.registries

import amf.core.services.{RuntimeCompiler, RuntimeValidator}
import amf.core.unsafe.PlatformSecrets
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies.RAMLVocabulariesPlugin
import amf.plugins.document.vocabularies.core.{
  DialectLanguageDefinition,
  DialectLoader,
  Vocabulary,
  VocabularyLanguageDefinition
}
import amf.plugins.document.vocabularies.spec.{Dialect, DialectNode, FragmentKind, ModuleKind}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Dialect registry.
  */
class DialectRegistry {

  protected var map: Map[String, Dialect] = Map()

  def knowsHeader(header: String): Boolean = map.contains(header.trim)

  def add(dialect: Dialect): DialectRegistry = {
    map = map + (dialect.header.replace("#", "") -> dialect)
    dialect.module.foreach { module =>
      val moduleHeader = "%RAML Library / " + dialect.header.substring(2)
      map = map + (moduleHeader -> Dialect(moduleHeader.substring(1),
                                           dialect.version,
                                           module,
                                           dialect.resolver,
                                           kind = ModuleKind))
    }
    dialect.fragments.foreach {
      case (k, fragment) =>
        val fragmentHeader = "%RAML " + k + " / " + dialect.header.substring(2)
        map = map + (fragmentHeader -> Dialect(fragmentHeader.substring(1),
                                               "",
                                               fragment,
                                               dialect.resolver,
                                               kind = FragmentKind))
    }
    if (!dialect.root.mappings().exists(x => x.name == "external")) {
      dialect.root.add(Vocabulary.externals.copy())
    }
    if (!dialect.root.mappings().exists(x => x.name == "uses")) {
      dialect.root.add(Vocabulary.externals.copy())
    }
    this
  }

  def get(h: String): Option[Dialect] = map.get(h.trim)

  def dialects: Seq[Dialect] = map.values.toSeq

  def knowsType(nodeType: String): Option[DialectNode] = knowsTypeInner(nodeType, dialects)

  private def knowsTypeInner(nodeType: String, dialects: Seq[Dialect]): Option[DialectNode] = {
    if (dialects.isEmpty) None
    else {
      dialects.head.knows(nodeType) match {
        case Some(dialectNode) => Some(dialectNode) // Some(DomainEntity(dialectNode))
        case None              => knowsTypeInner(nodeType, dialects.tail)
      }
    }
  }
}

object PlatformDialectRegistry extends DialectRegistry with PlatformSecrets {

  add(VocabularyLanguageDefinition)
  add(DialectLanguageDefinition)

  def registerDialect(uri: String): Future[Dialect] = {
    RuntimeValidator.disableValidationsAsync() { reenableValidations =>
      RuntimeCompiler(uri, platform, Option("application/yaml"), RAMLVocabulariesPlugin.ID)
        .map { compiled =>
          reenableValidations()
          val dialect = new DialectLoader(compiled).loadDialect()
          add(dialect)
          dialect
        }
    }
  }

  def registerDialect(url: String, dialectCode: String): Future[Dialect] = {
    platform.cacheResourceText(url, dialectCode)
    val res = registerDialect(url)
    platform.removeCacheResourceText(url)
    res
  }

  def registerNamespace(alias: String, prefix: String): Option[Namespace] = platform.registerNamespace(alias, prefix)
}
