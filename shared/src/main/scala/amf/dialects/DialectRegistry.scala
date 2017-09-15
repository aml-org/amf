package amf.dialects

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

  def get(h:String): Option[Dialect] = map.get(h.trim)
}


object DialectRegistry{
  val default = new DialectRegistry()
    .add(VocabularyLanguageDefinition)
    .add(DialectLanguageDefinition)
}
