package amf.model

import amf.model.builder.Builder

/**
  * Created by hernan.najles on 8/7/17.
  */
trait DomainElement {
  def toBuilder: Builder

}
