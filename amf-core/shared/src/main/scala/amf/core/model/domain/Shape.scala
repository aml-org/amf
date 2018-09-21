package amf.core.model.domain

import java.util.UUID

import amf.core.metamodel.domain.ShapeModel._
import amf.core.model.StrField
import amf.core.model.domain.extensions.{PropertyShape, ShapeExtension}
import amf.core.parser.ErrorHandler

import scala.collection.mutable

/**
  * Shape.
  */
abstract class Shape extends DomainElement with Linkable with NamedDomainElement {

  // used at runtime during validation
  val closureShapes: mutable.Set[Shape] = mutable.Set()


  def name: StrField                                     = fields.field(Name)
  def displayName: StrField                              = fields.field(DisplayName)
  def description: StrField                              = fields.field(Description)
  def default: DataNode                                  = fields.field(Default)
  def defaultString: StrField                            = fields.field(DefaultValueString)
  def values: Seq[DataNode]                              = fields.field(Values)
  def inherits: Seq[Shape]                               = fields.field(Inherits)
  def or: Seq[Shape]                                     = fields.field(Or)
  def and: Seq[Shape]                                    = fields.field(And)
  def xone: Seq[Shape]                                   = fields.field(Xone)
  def not: Shape                                         = fields.field(Not)
  def customShapeProperties: Seq[ShapeExtension]         = fields.field(CustomShapeProperties)
  def customShapePropertyDefinitions: Seq[PropertyShape] = fields.field(CustomShapePropertyDefinitions)
  // def closure: Seq[String]            = fields.field(Closure)

  def withName(name: String): this.type               = set(Name, name)
  def withDisplayName(name: String): this.type        = set(DisplayName, name)
  def withDescription(description: String): this.type = set(Description, description)
  def withDefault(default: DataNode): this.type       = set(Default, default)
  def withValues(values: Seq[String]): this.type      = set(Values, values)
  def withInherits(inherits: Seq[Shape]): this.type   = setArray(Inherits, inherits)
  def withOr(subShapes: Seq[Shape]): this.type        = setArray(Or, inherits)
  def withAnd(subShapes: Seq[Shape]): this.type       = setArray(And, inherits)
  def withXone(subShapes: Seq[Shape]): this.type      = setArray(Xone, inherits)
  def withNot(shape: Shape): this.type                = set(Not, shape)
  def withCustomShapeProperties(properties: Seq[ShapeExtension]): this.type =
    setArray(CustomShapeProperties, properties)
  def withCustomShapePropertyDefinitions(propertyDefinitions: Seq[PropertyShape]): this.type =
    setArray(CustomShapePropertyDefinitions, propertyDefinitions)

  def withCustomShapePropertyDefinition(name: String): PropertyShape = {
    val result = PropertyShape().withName(name)
    add(CustomShapePropertyDefinitions, result)
    result
  }
  /*
  def withClosure(closure: Seq[String]): this.type                     = set(Closure, closure)

  def appendToClosure(shapeId: String): this.type = {
    val updatedClosure = closure :+ shapeId
    withClosure(updatedClosure)
  }
  */
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
    } filter (_.id != id)
  }

  type FacetsMap = Map[String, PropertyShape]

  // @todo should be memoize this?
  def collectCustomShapePropertyDefinitions(onlyInherited: Boolean = false,
                                            traversed: mutable.Set[String] = mutable.Set()): Seq[FacetsMap] = {
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
          baseShape.collectCustomShapePropertyDefinitions(false, traversed += baseShape.id).flatMap {
            facetsMap: FacetsMap =>
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

  def cloneShape(recursionErrorHandler: Option[ErrorHandler],
                 recursionBase: Option[String] = None,
                 traversed: IdsTraversionCheck = IdsTraversionCheck()): Shape

  // Copy fields into a cloned shape
  protected def copyFields(recursionErrorHandler: Option[ErrorHandler],
                           cloned: Shape,
                           recursionBase: Option[String],
                           traversed: IdsTraversionCheck): Unit = {
    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case s: Shape if s.id != this.id && traversed.canTravers(s.id) =>
            traversed.runPushed((t: IdsTraversionCheck) => { s.cloneShape(recursionErrorHandler, recursionBase, t) })
          case s: Shape if s.id == this.id => s
          case a: AmfArray =>
            AmfArray(
              a.values.map {
                case e: Shape if e.id != this.id && traversed.canTravers(e.id) =>
                  traversed.runPushed((t: IdsTraversionCheck) => {
                    e.cloneShape(recursionErrorHandler, recursionBase, t)
                  })
//                e.cloneShape(recursionErrorHandler, recursionBase, traversed.push(prevBaseId),Some(prevBaseId))
                case e: Shape if e.id == this.id => e
                case o                           => o
              },
              a.annotations
            )
          case o => o
        }

        cloned.fields.setWithoutId(f, clonedValue, v.annotations)
    }
  }

  def ramlSyntaxKey: String = "shape"

  def copyShape(): this.type = copyElement().asInstanceOf[this.type]
}

case class IdsTraversionCheck() {

  private val backUps: mutable.Map[UUID, Set[String]]          = mutable.Map()
  private val ids: mutable.Set[String]                         = mutable.Set()
  private var whiteList: Set[String]                           = Set()
  private val whiteListBackUps: mutable.Map[UUID, Set[String]] = mutable.Map()

  private var allowedCycleClasses
    : Seq[Class[_]]                              = Seq() // i cant do it inmutable for the modularization (i cant see UnresolvedShape from here)
  private var stepOverFieldId: String => Boolean = (_: String) => false

  def withStepOverFunc(fn: String => Boolean): this.type = {
    stepOverFieldId = fn
    this
  }

  def withAllowedCyclesInstances(classes: Seq[Class[_]]): this.type = {
    allowedCycleClasses = classes
    this
  }

  def resetStepOverFun(): this.type = {
    stepOverFieldId = (_: String) => false
    this
  }

  def +(id: String): this.type = {
    ids += id
    this
  }

  def has(shape: Shape): Boolean =
    (!allowedCycleClasses.contains(shape.getClass)) && ids.contains(shape.id)

  def avoidError(id: String): Boolean = whiteList.contains(id)

  def avoidError(r: RecursiveShape, checkId: Option[String] = None): Boolean =
    avoidError(r.id) || avoidError(r.fixpoint.option().getOrElse("")) || (checkId.isDefined && avoidError(checkId.get))

  def hasId(id: String): Boolean = ids.contains(id)

  def canTravers(id: String): Boolean = !stepOverFieldId(id)

  private def push(): UUID = {
    val id = generateSha()
    backUps.put(id, ids.clone().toSet)
    id
  }

  def runWithIgnoredId(fnc: () => Shape, shapeId: String): Shape = runWithIgnoredIds(fnc, Set(shapeId))

  def runWithIgnoredIds(fnc: () => Shape, shapeIds: Set[String]): Shape = {
    val id = generateSha()
    whiteListBackUps.put(id, whiteList.toSet) // copy the whiteList set
    whiteList = whiteList ++ shapeIds
    val expanded = runPushed(_ => fnc())
    whiteList = whiteListBackUps(id)
    whiteListBackUps.remove(id)
    expanded
  }

  def recursionAllowed(fnc: () => Shape, shapeId: String): Shape = {
    val actual = ids.toSet + shapeId
    runWithIgnoredIds(fnc, actual)
  }

  def runPushed[T](fnc: (IdsTraversionCheck) => T): T = {
    val uuid    = push()
    val element = fnc(this)
    ids.clear()
    ids ++= backUps(uuid)
    backUps.remove(uuid)
    element
  }

  def generateSha(): UUID = UUID.randomUUID()

}
