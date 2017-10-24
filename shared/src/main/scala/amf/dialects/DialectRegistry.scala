package amf.dialects

import amf.compiler.RamlHeader
import amf.remote.Platform
import amf.spec.dialects.{Dialect, FragmentKind, ModuleKind}

import scala.concurrent.Future

/**
  * Created by Pavel Petrochenko on 14/09/17.
  */
class DialectRegistry {

  protected var map: Map[String, Dialect] = Map()

  def knowsHeader(header: RamlHeader): Boolean = knowsHeader(header.text)

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
        val fragmentHeader = "%RAML " + dialect.header.substring(2) + " / " + k
        map = map + (fragmentHeader -> Dialect(fragmentHeader.substring(1),
                                               "",
                                               fragment,
                                               dialect.resolver,
                                               kind = FragmentKind))
    }
    this
  }

  def get(h: String): Option[Dialect] = map.get(h.trim)

  def dialects: Seq[Dialect] = map.values.toSeq
}

abstract class PlatformDialectRegistry(p: Platform) extends DialectRegistry {

  add(VocabularyLanguageDefinition)
  add(DialectLanguageDefinition)

  def registerDialect(uri: String): Future[Dialect]
  def registerDialect(uri: String, dialect: String): Future[Dialect]
}

object DialectRegistry {
  val default: DialectRegistry = new DialectRegistry()
    .add(VocabularyLanguageDefinition)
    .add(DialectLanguageDefinition)
}
