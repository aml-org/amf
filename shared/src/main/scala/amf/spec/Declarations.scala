package amf.spec

import amf.document.BaseUnit
import amf.domain.`abstract`.{ResourceType, Trait}
import amf.domain.extensions.CustomDomainProperty
import amf.domain.{DomainElement, EndPoint, Operation}
import amf.shape.Shape

/**
  * Declarations object.
  */
case class Declarations(private val declarations: Seq[DomainElement], references: Map[String, BaseUnit] = Map()) {
  val shapes: Map[String, Shape] = declarations.collect { case d: Shape => d.name -> d }.toMap

  val annotations: Map[String, CustomDomainProperty] = declarations.collect {
    case d: CustomDomainProperty => d.name -> d
  }.toMap

  val resourceTypes: Map[String, ResourceType] = declarations.collect { case d: ResourceType => d.name -> d }.toMap

  val traits: Map[String, Trait] = declarations.collect { case d: Trait => d.name -> d }.toMap
}
