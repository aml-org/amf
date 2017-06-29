package amf.model

import amf.builder.{Builder, WebApiBuilder}

/**
  * WebApi domain model class. Root of the domain model.
  */
class WebApi(val name: String, val description: String, val host: String, val protocols: List[String])
    extends DomainElement
    with RootDomainElement {
  override def toBuilder[T <: DomainElement]: Builder[T] = ???
}
