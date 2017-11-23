package amf.model

import amf.framework.model.domain.Annotation
import amf.framework.parser.Annotations

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
