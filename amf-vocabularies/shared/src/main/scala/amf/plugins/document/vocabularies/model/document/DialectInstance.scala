package amf.plugins.document.vocabularies.model.document

import amf.core.metamodel.Obj
import amf.core.model.document.{BaseUnit, DeclaresModel, EncodesModel}
import amf.core.model.domain.{AmfObject, DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.document.vocabularies.metamodel.document.{DialectInstanceFragmentModel, DialectInstanceLibraryModel, DialectInstanceModel}
import amf.plugins.document.vocabularies.metamodel.document.DialectInstanceModel._
import amf.plugins.document.vocabularies.model.domain.DialectDomainElement

trait ComposedInstancesSupport {
  var composedDialects: Map[String, Dialect] = Map()

  def dialectForComposedUnit(dialect: Dialect) = composedDialects += (dialect.id -> dialect)
}

case class DialectInstance(fields: Fields, annotations: Annotations) extends BaseUnit with DeclaresModel with EncodesModel with ComposedInstancesSupport {

  override def meta: Obj = DialectInstanceModel

  def references: Seq[BaseUnit] = fields(References)
  def graphDependencies: Seq[String] = fields(GraphDependencies)
  def location: String = fields(Location)
  def encodes: DomainElement = fields(Encodes)
  def declares: Seq[DomainElement] = fields(Declares)
  def usage: String = fields(Usage)
  def adopted(parent: String): this.type = withId(parent)

  def definedBy(): String = fields(DefinedBy)
  def withDefinedBy(dialectId: String) = set(DefinedBy, dialectId)
  def withGraphDependencies(ids: Seq[String]) = set(GraphDependencies, ids)

  override def transform(selector: (DomainElement) => Boolean,
                transformation: (DomainElement, Boolean) => Option[DomainElement]): BaseUnit = {
    val domainElementAdapter = (o: AmfObject) => {
      o match {
        case e: DomainElement => selector(e)
        case _                => false
      }
    }
    val transformationAdapter = (o: AmfObject, isCycle: Boolean) => {
      o match {
        case e: DomainElement => transformation(e, isCycle)
        case _                => Some(o)
      }
    }
    transformByCondition(this, domainElementAdapter, transformationAdapter)
    this
  }

  override protected def transformByCondition(element: AmfObject,
                                              predicate: (AmfObject) => Boolean,
                                              transformation: (AmfObject, Boolean) => Option[AmfObject],
                                              cycles: Set[String] = Set.empty): AmfObject = {
    if (!cycles.contains(element.id)) {
      // not visited yet
      if (predicate(element)) { // matches predicate, we transform
        transformation(element, false).orNull
      } else {
        // not matches the predicate, we traverse

        element match {
          case dataNode: DialectDomainElement =>
            dataNode.objectProperties.foreach {
              case (prop, value) =>
                Option(transformByCondition(value, predicate, transformation, cycles + element.id)) match {
                  case Some(transformed: DialectDomainElement) =>
                    dataNode.objectProperties.put(prop, transformed)
                    dataNode
                  case _ =>
                    dataNode.objectProperties.remove(prop)
                    dataNode
                }
            }
            dataNode.objectCollectionProperties.foreach {
              case (prop, values: Seq[DialectDomainElement]) =>
                val newValues = values.map { value =>
                  Option(transformByCondition(value, predicate, transformation, cycles + element.id)) match {
                    case Some(transformed: DialectDomainElement) => Some(transformed)
                    case _                                       => None
                  }
                } collect { case Some(x) => x }
                if (newValues.isEmpty) {
                  dataNode.objectCollectionProperties.remove(prop)
                } else {
                  dataNode.objectCollectionProperties.put(prop, newValues)
                }
                dataNode
              case _ => dataNode
            }

          case other => super.transformByCondition(other, predicate, transformation, cycles)
        }
        element
      }

    } else
      element match {
        // target of the link has been traversed, we still visit the link in case a transformer wants to
        // transform links/references, but we will not traverse to avoid loops
        case linkable: Linkable if linkable.isLink =>
          if (predicate(element)) {
            transformation(element, true).orNull // passing the cycle boolean flat!
          } else {
            element
          }
        // traversed and not visited
        case _ => element
      }
  }
}

object DialectInstance {
  def apply(): DialectInstance = apply(Annotations())

  def apply(annotations: Annotations): DialectInstance = DialectInstance(Fields(), annotations)
}

case class DialectInstanceFragment(fields: Fields, annotations: Annotations) extends BaseUnit with EncodesModel with ComposedInstancesSupport {
  override def meta: Obj = DialectInstanceFragmentModel

  def references: Seq[BaseUnit] = fields(References)
  def graphDependencies: Seq[String] = fields(GraphDependencies)
  def location: String = fields(Location)
  def encodes: DomainElement = fields(Encodes)
    def usage: String = fields(Usage)
  def adopted(parent: String): this.type = withId(parent)

  def definedBy(): String = fields(DefinedBy)
  def withDefinedBy(dialectId: String) = set(DefinedBy, dialectId)
  def withGraphDepencies(ids: Seq[String]) = set(GraphDependencies, ids)
}

object DialectInstanceFragment {
  def apply(): DialectInstanceFragment = apply(Annotations())
  def apply(annotations: Annotations): DialectInstanceFragment = DialectInstanceFragment(Fields(), annotations)
}

case class DialectInstanceLibrary(fields: Fields, annotations: Annotations) extends BaseUnit with DeclaresModel with ComposedInstancesSupport {
  override def meta: Obj = DialectInstanceLibraryModel

  def references: Seq[BaseUnit] = fields(References)
  def graphDependencies: Seq[String] = fields(GraphDependencies)
  def location: String = fields(Location)
  def declares: Seq[DomainElement] = fields(Declares)
  def usage: String = fields(Usage)
  def adopted(parent: String): this.type = withId(parent)

  def definedBy(): String = fields(DefinedBy)
  def withDefinedBy(dialectId: String) = set(DefinedBy, dialectId)
  def withGraphDependencies(ids: Seq[String]) = set(GraphDependencies, ids)
}

object DialectInstanceLibrary {
  def apply(): DialectInstanceLibrary = apply(Annotations())
  def apply(annotations: Annotations): DialectInstanceLibrary = DialectInstanceLibrary(Fields(), annotations)
}