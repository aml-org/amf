package amf.spec

/**
  * Keeps context while parsing the model.
  */
case class ParserContext() {
  private var context: Map[ContextKey, Any] = Map()

  def set(key: ContextKey, any: Any): Unit = context = context + (key -> any)

  def apply(key: ContextKey): Option[Any] = context.get(key)

  def copy: ParserContext = {
    val c = ParserContext()
    c.context = Map() ++ context
    c
  }

  override def toString: String = s"ParserContext($context)"
}

trait ContextKey

object ContextKey {
  object EndPointBodyParameter  extends ContextKey
  object OperationBodyParameter extends ContextKey
}
