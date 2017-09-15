package amf.dialects

import amf.compiler.AMFCompiler
import amf.remote.{Platform, RamlYamlHint}

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

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

  def add(p: Platform, uri: String): Future[Dialect] = {
    AMFCompiler(uri, p, RamlYamlHint)
      .build()
      .map { compiled =>
        val dialect = new DialectLoader().loadDialect(compiled)
        add(dialect)
        dialect
      }
  }
}


object DialectRegistry{
  val default = new DialectRegistry()
    .add(VocabularyLanguageDefinition)
    .add(DialectLanguageDefinition)
}
