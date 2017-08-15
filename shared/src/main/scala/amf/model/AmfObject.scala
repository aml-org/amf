package amf.model

import amf.domain.Fields

/**
  * Created by pedro.colunga on 8/15/17.
  */
trait AmfObject extends AmfElement {

  /** Set of fields composing object. */
  val fields: Fields

  /** Return element unique identifier.*/
  def id: String = fields.id
}
