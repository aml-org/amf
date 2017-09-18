package amf.dialects

import amf.compiler.AMFCompiler
import amf.remote.{Platform, RamlYamlHint}
import scala.concurrent.Future
/**
  * Created by kor on 14/09/17.
  */
class DialectRegistry {

  private var map:Map[String,Dialect] = Map()

  def knowsHeader(h:String): Boolean = map.contains(h.trim)

  def add(dialect: Dialect): DialectRegistry = {
    map = map + (dialect.header -> dialect)
    this
  }

  def get(h: String): Option[Dialect] = map.get(h.trim)
}


abstract class PlatformDialectRegistry extends DialectRegistry {
  def add(p: Platform, uri: String): Future[Dialect]
}

object DialectRegistry{
  val default = new DialectRegistry()
    .add(VocabularyLanguageDefinition)
    .add(DialectLanguageDefinition)
}
