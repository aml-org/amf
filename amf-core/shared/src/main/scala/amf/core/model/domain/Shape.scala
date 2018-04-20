package amf.core.model.domain

import amf.core.metamodel.domain.ShapeModel._
import amf.core.model.StrField
import amf.core.model.domain.extensions.{PropertyShape, ShapeExtension}
import amf.core.parser.ErrorHandler

/**
  * Shape.
  */
abstract class Shape extends DomainElement with Linkable with NamedDomainElement {

  def name: StrField                                     = fields.field(Name)
  def displayName: StrField                              = fields.field(DisplayName)
  def description: StrField                              = fields.field(Description)
  def default: DataNode                                  = fields.field(Default)
  def defaultString: StrField                            = fields.field(DefaultValueString)
  def values: Seq[StrField]                              = fields.field(Values)
  def inherits: Seq[Shape]                               = fields.field(Inherits)
  def customShapeProperties: Seq[ShapeExtension]         = fields.field(CustomShapeProperties)
  def customShapePropertyDefinitions: Seq[PropertyShape] = fields.field(CustomShapePropertyDefinitions)

  def withName(name: String): this.type               = set(Name, name)
  def withDisplayName(name: String): this.type        = set(DisplayName, name)
  def withDescription(description: String): this.type = set(Description, description)
  def withDefault(default: DataNode): this.type       = set(Default, default)
  def withValues(values: Seq[String]): this.type      = set(Values, values)
  def withInherits(inherits: Seq[Shape]): this.type   = setArray(Inherits, inherits)
  def withCustomShapeProperties(properties: Seq[ShapeExtension]): this.type =
    setArray(CustomShapeProperties, properties)
  def withCustomShapePropertyDefinitions(propertyDefinitions: Seq[PropertyShape]): this.type =
    setArray(CustomShapePropertyDefinitions, propertyDefinitions)

  def withCustomShapePropertyDefinition(name: String): PropertyShape = {
    val result = PropertyShape().withName(name)
    add(CustomShapePropertyDefinitions, result)
    result
  }

  def withDefaultStr(value: String): Shape.this.type = set(DefaultValueString, value)

  def effectiveInherits: Seq[Shape] = {
    inherits.map { base =>
      if (base.linkTarget.isDefined) {
        base.effectiveLinkTarget match {
          case linkedShape: Shape => linkedShape
          case _                  => base // TODO: what should we do here?
        }
      } else {
        base
      }
    } filter(_.id != id)
  }

  type FacetsMap = Map[String, PropertyShape]

  // @todo should be memoize this?
  def collectCustomShapePropertyDefinitions(onlyInherited: Boolean = false, traversed: Set[String] = Set.empty): Seq[FacetsMap] = {
    // Facet properties for the current shape
    val accInit: FacetsMap = Map.empty
    val initialSequence = if (onlyInherited) {
      Seq(accInit)
    } else {
      Seq(customShapePropertyDefinitions.foldLeft(accInit) { (acc: FacetsMap, propertyShape: PropertyShape) =>
        acc.updated(propertyShape.name.value(), propertyShape)
      })
    }

    // Check in the inheritance chain to add properties coming from super shapes and merging them with the facet
    // properties or properties for the current shape.
    // Notice that the properties map for this shape or from the inheritance can be sequences with more than one
    // element if unions are involved
    // inheritance will get the map of facet properties for each element in the union
    if (inherits.nonEmpty) {
      // for each base shape compute sequence(s) of facets map and merge it with the
      // initial facets maps computed for this shape. This multiplies the number of
      // final facets maps
      effectiveInherits.foldLeft(initialSequence) { (acc: Seq[FacetsMap], baseShape: Shape) =>
        if (!traversed.contains(baseShape.id)) {
          baseShape.collectCustomShapePropertyDefinitions(false, traversed + baseShape.id).flatMap { facetsMap: FacetsMap =>
            acc.map { accFacetsMap =>
              accFacetsMap ++ facetsMap
            }
          }
        } else {
          acc
        }
      }
    } else {
      // no inheritance, return the initial sequence
      initialSequence
    }
  }

  def cloneShape(recursionErrorHandler: Option[ErrorHandler], recursionBase: Option[String] = None, traversed: Set[String] = Set()): Shape

  // Copy fields into a cloned shape
  protected def copyFields(recursionErrorHandler: Option[ErrorHandler], cloned: Shape, recursionBase: Option[String], traversed: Set[String]): Unit = {
    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case s: Shape if s.id != this.id => s.cloneShape(recursionErrorHandler, recursionBase, traversed)
          case s: Shape if s.id == this.id => s
          case a: AmfArray =>
            AmfArray(a.values.map {
              case e: Shape if e.id != this.id => e.cloneShape(recursionErrorHandler, recursionBase, traversed)
              case e: Shape if e.id == this.id => e
              case o                           => o
            }, a.annotations)
          case o => o
        }

        cloned.fields.setWithoutId(f, clonedValue, v.annotations)
    }
  }
}
