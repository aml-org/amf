package amf.graph

import amf.vocabulary.{Namespace, ValueType}

/**
  * Graph context
  */
trait GraphContext {
  def expand(iri: String): String
  def reduce(value: ValueType): String
  def mappings(each: ((String, Namespace)) => Unit): Unit
}

private case class MapGraphContext(context: Map[String, Namespace]) extends GraphContext {

  override def expand(iri: String): String = {
    iri.split(":") match {
      case Array(alias, name) =>
        context.get(alias) match {
          case Some(namespace) => namespace.base + name
          case None            => undefined(iri, alias)
        }
      case _ => iri
    }
  }

  private val reversed = context.map(_.swap)

  override def reduce(value: ValueType): String = {
    reversed.get(value.ns) match {
      case Some(alias) => alias + ":" + value.name
      case None        => undefined(value.iri(), value.ns)
    }
  }

  private def undefined(iri: String, ns: Any) = {
    throw new Exception(s"Undefined namespace $ns for $iri. Defined context: $context")
  }

  override def mappings(each: ((String, Namespace)) => Unit): Unit = context.foreach(each)
}

private object EmptyGraphContext extends GraphContext {
  override def expand(iri: String): String                         = iri
  override def reduce(value: ValueType): String                    = value.iri()
  override def mappings(each: ((String, Namespace)) => Unit): Unit = {}
}
