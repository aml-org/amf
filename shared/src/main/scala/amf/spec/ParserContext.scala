package amf.spec

/**
  * Keeps context while parsing the model.
  */
case class ParserContext(var context: Map[ContextKey, Any] = Map()) {

  def set(key: ContextKey, any: Any): Unit = context = context + (key -> any)

  def apply(key: ContextKey): Option[Any] = context.get(key)

  override def toString: String = s"ParserContext($context)"
}

trait ContextKey

object ContextKey {
  object EndPointBodyParameter  extends ContextKey
  object OperationBodyParameter extends ContextKey
}
