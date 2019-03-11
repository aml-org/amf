package amf.core.parser

class ContextVariables {
  private var variables: Map[String, Any] = Map()

  def get[T <: Any](key: String): Option[T] = variables.get(key).map(_.asInstanceOf[T])

  def contains(key: String): Boolean = get(key).isDefined

  def +=(key: String, value: Any): this.type = {
    variables += (key -> value)
    this
  }

  def ++=(other: ContextVariables): this.type = this ++= other.variables

  def ++=(other: TraversableOnce[(String, Any)]): this.type = {
    variables ++= other
    this
  }

  def copy(): ContextVariables = ContextVariables(this)
}

object ContextVariables {
  def apply(): ContextVariables = new ContextVariables()

  def apply(variables: ContextVariables): ContextVariables = {
    val result = new ContextVariables()
    result.variables ++= variables.variables
    result
  }
}
