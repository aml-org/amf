package amf.dialects

import amf.compiler.AMFCompiler
import amf.remote.{Platform, RamlYamlHint}
import amf.spec.dialects.Dialect

import scala.concurrent.Future

/**
  * Created by Pavel Petrochenko on 14/09/17.
  */
class DialectRegistry {

  protected var map: Map[String,Dialect] = Map()

  def knowsHeader(h: String): Boolean = map.contains(h.trim)

  def add(dialect: Dialect): DialectRegistry = {
    map = map + (dialect.header -> dialect)
    this
  }

  def get(h: String): Option[Dialect] = map.get(h.trim)
}


abstract class PlatformDialectRegistry(p: Platform) extends DialectRegistry {

  add(VocabularyLanguageDefinition)
  add(DialectLanguageDefinition)

  def registerDialect(uri: String): Future[Dialect]
}

object DialectRegistry {
  val default: DialectRegistry = new DialectRegistry()
    .add(VocabularyLanguageDefinition)
    .add(DialectLanguageDefinition)
}
