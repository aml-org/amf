package amf.domain.dialects

import amf.domain.{Annotations, DomainElement, Fields}
import amf.model.{AmfArray, AmfElement, AmfScalar}
import amf.spec.dialects.{DialectNode, DialectPropertyMapping, DomainEntityVisitor}

case class DomainEntity(linkValue: Option[String], definition: DialectNode, fields: Fields, annotations: Annotations) extends DomainElement {

  override def adopted(parent: String): this.type = {
    if (Option(this.id).isEmpty) {
      linkValue match {
        case Some(link) =>
          parent.charAt(parent.length - 1) match {
            case '/' => withId(s"$parent$link")
            case '#' => withId(s"$parent$link")
            case _   => withId(s"$parent/$link")
          }
        case _          =>
          withId(parent)
      }
    }
    this
  }

  def traverse(visitor: DomainEntityVisitor): Unit =
      definition.mappings().foreach { mapping =>
        if (!mapping.isScalar) {
          val element = fields.get(mapping.field())
          element match {
            case array: AmfArray => array.values.foreach(visitElement(visitor, mapping, _))
            case _ =>
          }
          visitElement(visitor, mapping, element)
        }
      }

  private def visitElement(visitor: DomainEntityVisitor, mapping: DialectPropertyMapping, element: AmfElement): Unit =
    element match {
      case domainEntity: DomainEntity =>
        val visitChidren = visitor.visit(domainEntity, mapping)
        if (visitChidren) { domainEntity.traverse(visitor) }
      case _ => // ignore
    }

  def boolean(m: DialectPropertyMapping): Option[Boolean] =
    fields.get(m.field()) match {
      case scalar: AmfScalar => Option(scalar.toString.toBoolean)
      case _                 => None
    }

  def string(m: DialectPropertyMapping): Option[String] =
    this.fields.get(m.field()) match {
      case scalar: AmfScalar => Some(scalar.toString)
      case _                 => None
    }

  def addValue(mapping: DialectPropertyMapping, value: String): Unit = add(mapping.field(), AmfScalar(value))

  private def mappingToStrings(m :DialectPropertyMapping): Seq[Option[String]] =
    this.fields.get(m.field()) match {
      case scalar: AmfScalar => List(Some(scalar.toString))

      case array: AmfArray   => array.values.map {
        case scalarMember: AmfScalar => Some(scalarMember.toString)
        case _ => None
      }

      case _                 => List.empty
    }
  def strings(m: DialectPropertyMapping): Seq[String] = mappingToStrings(m).filter(_.isDefined).map(_.get)
  def rawstrings(m: DialectPropertyMapping): Seq[String] = mappingToStrings(m).map(_.getOrElse(""))

  def entity(m: DialectPropertyMapping): Option[DomainEntity] =
    fields.get(m.field()) match {
      case entity: DomainEntity => Some(entity)
      case _                    => None
    }


  def entities(m:DialectPropertyMapping):Seq[DomainEntity] =
    fields.get(m.field()) match {
      case entity: DomainEntity => List(entity)
      case array: AmfArray      => array.values.filter(_.isInstanceOf[DomainEntity]).asInstanceOf[List[DomainEntity]]
      case _                    => List()
    }

  def mapElementWithId(m:DialectPropertyMapping,id:String): Option[DomainEntity] ={
    fields.get(m.field()) match {
      case array: AmfArray =>
        array
          .values
          .filter(v => v.isInstanceOf[DomainEntity])
          .find { case x: DomainEntity => x.id == id }
          .asInstanceOf[Option[DomainEntity]]
      case _ => None
    }
  }

  override def dynamicTypes(): Seq[String] = definition.calcTypes(this).map(_.iri())
}

object DomainEntity{
  def apply(d:DialectNode): DomainEntity = DomainEntity(None, d, Fields(), Annotations())
}