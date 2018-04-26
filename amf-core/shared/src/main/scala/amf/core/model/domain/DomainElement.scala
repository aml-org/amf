package amf.core.model.domain

import amf.core.annotations.LexicalInformation
import amf.core.metamodel.Obj
import amf.core.metamodel.domain.DomainElementModel._
import amf.core.model.domain.extensions.DomainExtension
import amf.core.parser.Range

/**
  * Internal model for any domain element
  */
trait DomainElement extends AmfObject {

  def meta: Obj

  def customDomainProperties: Seq[DomainExtension] = fields.field(CustomDomainProperties)
  def extend: Seq[DomainElement]                   = fields.field(Extends)

  def withCustomDomainProperties(extensions: Seq[DomainExtension]): this.type =
    setArray(CustomDomainProperties, extensions)

  def withCustomDomainProperty(extensions: DomainExtension): this.type =
    add(CustomDomainProperties, extensions)

  def withExtends(extend: Seq[DomainElement]): this.type = setArray(Extends, extend)

  def position(): Option[Range] = annotations.find(classOf[LexicalInformation]) match {
    case Some(info) => Some(info.range)
    case _          => None
  }

  lazy val graph: Graph = Graph(this)

}
