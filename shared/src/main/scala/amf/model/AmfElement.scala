package amf.model

import amf.domain.{Annotation, Annotations, Fields}

/**
  * Amf element including DomainElements and BaseUnits
  */
trait AmfElement {

  /** Set of annotations for element. */
  val annotations: Annotations
}
