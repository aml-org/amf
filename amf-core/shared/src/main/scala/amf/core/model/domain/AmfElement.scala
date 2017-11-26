package amf.core.model.domain

import amf.core.parser.Annotations

/**
  * Amf element including DomainElements and BaseUnits
  */
trait AmfElement {

  /** Set of annotations for element. */
  val annotations: Annotations

  /** Add specified annotation. */
  def add(annotation: Annotation): this.type = {
    annotations += annotation
    this
  }

  /** Merge specified annotations. */
  def add(other: Annotations): this.type = {
    annotations ++= other
    this
  }
}
