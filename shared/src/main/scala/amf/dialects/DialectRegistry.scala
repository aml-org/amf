package amf.dialects

/**
  * Created by kor on 14/09/17.
  */
class DialectRegistry {

  private var map:Map[String,Dialect] = Map()

  def knowsHeader(h:String): Boolean = map.contains(h.trim)

  def add(dialect: Dialect): Unit = {
    map = map + ("#%RAML 1.0 " + dialect.name -> dialect)
  }

  def get(h:String): Option[Dialect] = map.get(h.trim)
}


object DialectRegistry{

  val default = new DialectRegistry()

  default.add(VocabularyLanguageDefinition)
  default.add(DialectLanguageDefinition)
}
