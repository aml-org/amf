package amf.plugins.domain.shapes.models

import amf.domain.extensions.ShapeExtension
import amf.domain.{Example, Linkable}
import amf.framework.model.domain.DomainElement
import amf.model.AmfArray
import amf.plugins.document.webapi.annotations.{ExplicitField, ParsedFromTypeExpression}
import amf.plugins.domain.shapes.metamodel.ShapeModel._
import amf.plugins.domain.webapi.models.CreativeWork

/**
  * Shape.
  */
abstract class Shape extends DomainElement with Linkable {

  def name: String                               = fields(Name)
  def displayName: String                        = fields(DisplayName)
  def description: String                        = fields(Description)
  def default: String                            = fields(Default)
  def values: Seq[String]                        = fields(Values)
  def documentation: CreativeWork                = fields(Documentation)
  def xmlSerialization: XMLSerializer            = fields(XMLSerialization)
  def inherits: Seq[Shape]                       = fields(Inherits)
  def examples: Seq[Example]                     = fields(Examples)
  def customShapeProperties: Seq[ShapeExtension] = fields(CustomShapeProperties)
  def customShapePropertyDefinitions: Seq[PropertyShape] = fields(CustomShapePropertyDefinitions)

  def withName(name: String): this.type                                     = set(Name, name)
  def withDisplayName(name: String): this.type                              = set(DisplayName, name)
  def withDescription(description: String): this.type                       = set(Description, description)
  def withDefault(default: String): this.type                               = set(Default, default)
  def withValues(values: Seq[String]): this.type                            = set(Values, values)
  def withDocumentation(documentation: CreativeWork): this.type             = set(Documentation, documentation)
  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type      = set(XMLSerialization, xmlSerialization)
  def withInherits(inherits: Seq[Shape]): this.type                         = setArray(Inherits, inherits)
  def withExamples(examples: Seq[Example]): this.type                       = setArray(Examples, examples)
  def withCustomShapeProperties(properties: Seq[ShapeExtension]): this.type = setArray(CustomShapeProperties, properties)
  def withCustomShapePropertyDefinitions(propertyDefinitions: Seq[PropertyShape]): this.type = setArray(CustomShapePropertyDefinitions, propertyDefinitions)


  def fromTypeExpression: Boolean = this.annotations.contains(classOf[ParsedFromTypeExpression])
  def typeExpression: String = this.annotations.find(classOf[ParsedFromTypeExpression]) match {
    case Some(expr: ParsedFromTypeExpression) => expr.value
    case _                                    => throw new Exception("Trying to extract non existent type expression")
  }

  def withCustomShapePropertyDefinition(name: String): PropertyShape = {
    val result = PropertyShape().withName(name)
    add(CustomShapePropertyDefinitions, result)
    result
  }

  def cloneShape(): this.type = {
    val cloned = this match {
      case _: UnionShape    => UnionShape()
      case _: ScalarShape   => ScalarShape()
      case _: ArrayShape    => ArrayShape()
      case _: MatrixShape   => MatrixShape()
      case _: TupleShape    => TupleShape()
      case _: PropertyShape => PropertyShape()
      case _: FileShape     => FileShape()
      case _: AnyShape      => AnyShape()
      case _: NilShape      => NilShape()
      case _: NodeShape     => NodeShape()
    }
    cloned.id = this.id
    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case s: Shape => s.cloneShape()
          case a: AmfArray =>
            AmfArray(a.values.map {
              case e: Shape => e.cloneShape()
              case o        => o
            }, a.annotations)
          case o => o
        }

        cloned.fields.setWithoutId(f, clonedValue, v.annotations)
    }
    if (cloned.isInstanceOf[NodeShape]) {
      cloned.add(ExplicitField())
    }
    cloned.asInstanceOf[this.type]
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


    // Check in the inheritance chain to add properties comming from super shapes and merging them with the facet
    // properties or properties for the current shape.
    // Notice that the properties map for this shape or from the inheritance can be sequences with more than one
    // element if unions are involved
    Option(inherits) match {
      // inheritance well get the map of facet properties for each element in the union
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

}
