package amf.model

import amf.domain.{Annotation, Fields}

/**
  * Amf element including DomainElements and BaseUnits
  */
trait AmfElement {

  /** Set of fields composing object. */
  val fields: Fields

  /** Set of annotations for object. */
  val annotations: List[Annotation]

  /** Return element unique identifier.*/
  def id: String = fields.id
}
