package amf.spec

import amf.document.Fragment.Fragment
import amf.document.{BaseUnit, Module}
import amf.domain.DomainElement
import amf.domain.`abstract`.{ResourceType, Trait}
import amf.domain.extensions.CustomDomainProperty
import amf.shape.Shape

import scala.collection.mutable

/**
  * Declarations object.
  */
case class Declarations(private val declarations: Seq[DomainElement],
                        private val references: Map[String, DomainElement]) {
  val shapes: Map[String, Shape] = declarations.collect { case d: Shape => d.name -> d }.toMap

  val annotations: Map[String, CustomDomainProperty] = declarations.collect {
    case d: CustomDomainProperty => d.name -> d
  }.toMap

  val resourceTypes: Map[String, ResourceType] = declarations.collect { case d: ResourceType => d.name -> d }.toMap

  val traits: Map[String, Trait] = declarations.collect { case d: Trait => d.name -> d }.toMap

  def find(key: String): Option[DomainElement] = {
    (shapes ++ annotations ++ resourceTypes ++ traits ++ references).get(key)
//      .get(key)
//      .fold({
//        annotations
//          .get(key)
//          .fold({
//            resourceTypes.get(key)
//          })(s => Some(s))
//          .fold({
//            references
//              .get(key)
//          })(s => Some(s))
//      })(s => Some(s))
  }

  def add(elements: Seq[DomainElement]): Declarations =
    this.copy(declarations = this.declarations ++ elements)
}

object Declarations {

  def apply(): Declarations = apply(Nil)

  def apply(map: Map[String, BaseUnit]): Declarations = new Declarations(Nil, plaintReferences(map))

  def apply(declarations: Seq[DomainElement]): Declarations = new Declarations(declarations, Map())

  def plaintReferences(ref: Map[String, BaseUnit]): Map[String, DomainElement] = {
    val plained = mutable.Map[String, DomainElement]()

    ref.foreach({
      case (k: String, m: Module)   => plained ++= plainModule(k, m)
      case (k: String, f: Fragment) => plained += k -> f.encodes
      case _                        =>
    })
    plained.toMap
  }

  def plainModule(prefix: String, m: Module): Map[String, DomainElement] = {
    def addPrefix(name: String): String = prefix + "." + name

    m.declares
      .map({
        case d: Shape                => addPrefix(d.name) -> d
        case c: CustomDomainProperty => addPrefix(c.name) -> c // todo other domain element root types
      })
      .toMap
  }
}
