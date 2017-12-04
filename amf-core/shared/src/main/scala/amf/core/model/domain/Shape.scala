package amf.core.model.domain

import amf.core.metamodel.domain.ShapeModel._
import amf.core.model.domain.extensions.{PropertyShape, ShapeExtension}
/**
  * Shape.
  */
abstract class Shape extends DomainElement with Linkable {

  def name: String                               = fields(Name)
  def displayName: String                        = fields(DisplayName)
  def description: String                        = fields(Description)
  def default: String                            = fields(Default)
  def values: Seq[String]                        = fields(Values)
  def inherits: Seq[Shape]                       = fields(Inherits)
  def customShapeProperties: Seq[ShapeExtension] = fields(CustomShapeProperties)
  def customShapePropertyDefinitions: Seq[PropertyShape] = fields(CustomShapePropertyDefinitions)

  def withName(name: String): this.type                                     = set(Name, name)
  def withDisplayName(name: String): this.type                              = set(DisplayName, name)
  def withDescription(description: String): this.type                       = set(Description, description)
  def withDefault(default: String): this.type                               = set(Default, default)
  def withValues(values: Seq[String]): this.type                            = set(Values, values)
  def withInherits(inherits: Seq[Shape]): this.type                         = setArray(Inherits, inherits)
  def withCustomShapeProperties(properties: Seq[ShapeExtension]): this.type = setArray(CustomShapeProperties, properties)
  def withCustomShapePropertyDefinitions(propertyDefinitions: Seq[PropertyShape]): this.type = setArray(CustomShapePropertyDefinitions, propertyDefinitions)



  def withCustomShapePropertyDefinition(name: String): PropertyShape = {
    val result = PropertyShape().withName(name)
    add(CustomShapePropertyDefinitions, result)
    result
  }

  type FacetsMap = Map[String, PropertyShape]

  // @todo should be memoize this?
  def collectCustomShapePropertyDefinitions(onlyInherited: Boolean = false): Seq[FacetsMap] = {
    // Facet properties for the current shape
    val accInit: FacetsMap = Map.empty
    val initialSequence =  if (onlyInherited) {
      Seq(accInit)
    } else {
      Seq(customShapePropertyDefinitions.foldLeft(accInit) { (acc: FacetsMap, propertyShape: PropertyShape) =>
        acc.updated(propertyShape.name, propertyShape)
      })
    }

    // Check in the inheritance chain to add properties coming from super shapes and merging them with the facet
    // properties or properties for the current shape.
    // Notice that the properties map for this shape or from the inheritance can be sequences with more than one
    // element if unions are involved
    Option(inherits) match {
      // inheritance will get the map of facet properties for each element in the union
      case Some(baseShapes: Seq[Shape]) =>

        // for each base shape compute sequence(s) of facets map and merge it with the
        // initial facets maps computed for this shape. This multiplies the number of
        // final facets maps
        baseShapes.foldLeft(initialSequence) { (acc: Seq[FacetsMap], baseShape: Shape) =>
          baseShape.collectCustomShapePropertyDefinitions().flatMap { facetsMap: FacetsMap =>
            acc.map { accFacetsMap => accFacetsMap ++ facetsMap }
          }
        }

      // no inheritance, return the initial sequence
      case _ => initialSequence
    }
  }

  def cloneShape(recursionBase: Option[String] = None): Shape


  // Copy fields into a cloned shape
  protected def copyFields(cloned: Shape, recursionBase: Option[String]) = {
    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case s: Shape => s.cloneShape(recursionBase)
          case a: AmfArray =>
            AmfArray(a.values.map {
              case e: Shape => e.cloneShape(recursionBase)
              case o        => o
            }, a.annotations)
          case o => o
        }

        cloned.fields.setWithoutId(f, clonedValue, v.annotations)
    }
  }
}
