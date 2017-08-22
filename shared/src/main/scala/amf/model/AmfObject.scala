package amf.model

import amf.domain.{Annotations, Fields}
import amf.metadata.Field

/**
  * Created by pedro.colunga on 8/15/17.
  */
trait AmfObject extends AmfElement {

  /** Set of fields composing object. */
  val fields: Fields

  /** Return element unique identifier. */
  def id: String = fields.id

  /** Set element unique identifier. */
  def withId(id: String): this.type = {
    fields.id = id
    this
  }

  /** Call after object has been adopted by specified parent. */
  def adopted(parent: String): this.type

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

  /** Add field value to array. */
  def add(field: Field, value: AmfElement): this.type = {
    fields.add(field, value)
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
}
