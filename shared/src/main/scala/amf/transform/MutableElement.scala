package amf.transform

import amf.domain.{Annotation, Annotations, Fields}
import amf.metadata.Field
import amf.model.{AmfArray, AmfElement, AmfScalar}

/**
  * Created by pedro.colunga on 8/15/17.
  */
trait MutableElement {

  /** Set of fields composing element. */
  val fields: Fields

  /** Element annotations. */
  val annotations: Annotations

  /** Set scalar value. */
  def set(field: Field, value: String): this.type = set(field, AmfScalar(value))

  /** Set scalar value. */
  def set(field: Field, value: Boolean): this.type = set(field, AmfScalar(value))

  /** Set scalar value. */
  def set(field: Field, values: Seq[String]): this.type = setArray(field, values.map(AmfScalar(_)))

  /** Set field value. */
  def set(field: Field, value: AmfElement): this.type = {
    fields.set(field, value)
    this
  }

  /** Set field value. */
  def setArray(field: Field, values: Seq[AmfElement]): this.type = {
    fields.set(field, AmfArray(values))
    this
  }

  /** Set field value. */
  def set(field: Field, value: AmfElement, annotations: Annotations): this.type = {
    fields.set(field, value, annotations)
    this
  }

  def add(annotation: Annotation): this.type = {
    annotations += annotation
    this
  }
}
