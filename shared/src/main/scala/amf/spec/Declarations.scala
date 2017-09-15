package amf.spec

import amf.domain.DomainElement
import amf.domain.extensions.CustomDomainProperty
import amf.shape.Shape

/**
  * Declarations object.
  */
case class Declarations(private val declarations: Seq[DomainElement]) {
  val shapes: Map[String, Shape] = declarations.collect { case d: Shape => d.name -> d }.toMap
  val annotations: Map[String, CustomDomainProperty] = declarations.collect {
    case d: CustomDomainProperty => d.name -> d
  }.toMap
}
