package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain._
import amf.core.internal.annotations._
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.validation.CoreValidations.RecursiveShapeSpecification
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.metamodel.UnionShapeModel.AnyOf
import amf.shapes.internal.domain.metamodel._

import scala.collection.mutable

case class ShapeNormalizationInheritanceResolver(context: NormalizationContext) {

  private val visitedIds = mutable.ArrayBuffer[Shape]()

  // This variable is used to back track
  private var detectedRecursion = false

  // ID of the shape where inheritance recursion was detected
  private var recursionGenerator = "fakeId"

  private def addToCache(shape: Shape, id: String) = context.resolvedInheritanceCache += (shape, id)
  private def addToCache(shape: Shape)             = context.resolvedInheritanceCache += shape

  private def withVisitTracking[T](shape: Shape)(func: () => T) = {
    visitedIds += shape
    val result = func()
    visitedIds.remove(visitedIds.size - 1)
    result
  }

  def normalize(shape: Shape): Shape = {
    context.resolvedInheritanceCache.get(shape.id) match {
      case Some(resolvedInheritance) => resolvedInheritance
      case _ =>
        val result = normalizeAction(shape)
        result
    }
  }

  private def normalizeAction(shape: Shape): Shape = {
    if (isPartOfInheritanceCycle(shape)) {
      invalidRecursionError(shape)
      markInheritanceRecursionDetected(shape)
      shape
    } else if (hasSuperTypes(shape)) {
      val resolvedShape = withVisitTracking(shape) { () =>
        resolveInheritance(shape)
      }
//      addToCache(resolvedShape)
      resolvedShape
    } else {
      shape
    }
  }
  private def markInheritanceRecursionDetected(shape: Shape): Unit = {
    detectedRecursion = true
    recursionGenerator = shape.id
  }

  private def resolveInheritance(shape: Shape): Shape = {
    if (canReplaceForInherits(shape)) {
      applySimpleInheritance(shape)
    } else {
      val superTypes    = shape.inherits
      val resolvedShape = inheritFromSuperTypes(shape, superTypes)

      // Erasing this has no effect on tests failing / passing (Tomi)
      // Reset when we return to the first Shape of the cycle
      if (detectedRecursion && shape.id == recursionGenerator) detectedRecursion = false

      // This is necessary due to a limitation we have with examples in Restriction Computation (what is this limitation)
      // Shouldn't be here
      shape match {
        case any: AnyShape if isSimpleInheritance(any, superTypes) =>
          ExamplesCopier(normalize(superTypes.head), resolvedShape)
        case _ => // Nothing to do
      }

      addToCache(resolvedShape)

      resolvedShape match {
        case u: UnionShape =>
          val originalAnnotations = u.fields.getValueAsOption(AnyOf) match {
            case Some(value) => value.annotations
            case None        => Annotations()
          }



          val resolvedAnyOf = u.anyOf.map {
            case s if s.inherits.nonEmpty => ShapeNormalizationInheritanceResolver(context).normalize(s)
            case s                        => s
          }

          u.setArrayWithoutId(AnyOf, resolvedAnyOf, originalAnnotations)
        case _ => // ignore
      }

      resolvedShape
    }
  }

  private def inheritFromSuperTypes(shape: Shape, superTypes: Seq[Shape]) = {
    shape.fields.removeField(ShapeModel.Inherits)
    superTypes.fold(shape) { (accShape, superType) =>
      // go up the inheritance chain before applying type. We want to apply inheritance with the accumulated super type
      val normalizedSuperType = normalize(superType)
      if (detectedRecursion) accShape
      else {
        val r = context.minShape(accShape, normalizedSuperType)
        if (context.keepEditingInfo && !wasSimpleUnionInheritance(accShape, r, normalizedSuperType))
          withInheritanceAnnotation(r, normalizedSuperType)
        else r
      }
    }
  }

  private def wasSimpleUnionInheritance(original: Shape, newShapeAfterInheritance: Shape, parent: Shape): Boolean = {
    !original.isInstanceOf[UnionShape] &&
    newShapeAfterInheritance.isInstanceOf[UnionShape] &&
    parent.isInstanceOf[UnionShape]
  }
  private def withInheritanceAnnotation(child: Shape, parent: Shape): Shape = {
    val startingValue = child.annotations.find(classOf[InheritedShapes]) match {
      case Some(oldAnnotation) => oldAnnotation.baseIds
      case None                => Nil
    }
    child.annotations.reject(_.isInstanceOf[InheritedShapes]) += InheritedShapes(
      startingValue :+ parent.id
    ) // maybe optimizable, do no create and reject annotations all the time
    child
  }

  private def applySimpleInheritance(shape: Shape) = {
    // Check if we should clone here
    val referencedShape = shape.inherits.head
    shape.fields.removeField(ShapeModel.Inherits)
    val resolvedShape = normalize(referencedShape)
    if (hasAutoGeneratedName(shape)) referencedShape.add(AutoGeneratedName())
    ExamplesCopier(shape, resolvedShape)
    addToCache(resolvedShape, shape.id)
    resolvedShape
  }

  private def hasAutoGeneratedName(shape: Shape) = shape.annotations.contains(classOf[AutoGeneratedName])

  private def hasSuperTypes(shape: Shape) = shape.inherits.nonEmpty

  private def isSimpleInheritance(shape: Shape, superTypes: Seq[Shape] = Seq()): Boolean = {
    shape match {
      case ns: NodeShape => superTypes.size == 1 && isDeclaredElement(ns) && ns.properties.isEmpty
      case _: AnyShape if superTypes.size == 1 =>
        val superType = superTypes.head
        val ignoredFields =
          Seq(
            ShapeModel.Inherits,
            ShapeModel.Name,
            ShapeModel.DisplayName,
            ShapeModel.Description,
            AnyShapeModel.Examples,
            AnyShapeModel.Documentation,
            AnyShapeModel.Comment
          )
        allFieldsMatchSuperTypes(shape, superType, ignoredFields)
      case _ => false
    }
  }

  private def isDeclaredElement(ns: DomainElement) = ns.annotations.contains(classOf[DeclaredElement])

  private def canReplaceForInherits(shape: Shape): Boolean = shape match {
    case anyShape: AnyShape if isDeclaredElement(anyShape) => false
    case anyShape: AnyShape if anyShape.inherits.size == 1 =>
      val superType     = anyShape.inherits.head
      val ignoredFields = Seq(ShapeModel.Inherits, AnyShapeModel.Examples, AnyShapeModel.Name)
      allFieldsMatchSuperTypes(shape, superType, ignoredFields)
    case _ => false
  }

  private def allFieldsMatchSuperTypes(shape: Shape, superType: Shape, ignoredFields: Seq[Field] = Seq()): Boolean = {
    val effectiveFields = shape.fields.fields().filterNot(f => ignoredFields.contains(f.field))
    // To be a simple inheritance, all the effective fields of the shape must be the same in the superType
    effectiveFields.foreach(e => {
      superType.fields.entry(e.field) match {
        case Some(s) if s.value.value.equals(e.value.value) => // Valid
        case _ if e.field == NodeShapeModel.Closed          => // Valid
        case _                                              => return false
      }
    })
    true
  }

  private def isPartOfInheritanceCycle(shape: Shape) = visitedIds.exists(_.id == shape.id)

  private def invalidRecursionError(lastVersion: Shape): Unit = {
    val chain = visitedIds.map(_.name.value()).mkString(" -> ") + s" -> ${lastVersion.name.value()}"
    context.errorHandler.violation(
      RecursiveShapeSpecification,
      lastVersion.id,
      None,
      s"Cyclic inheritance: $chain",
      lastVersion.position(),
      lastVersion.location()
    )
  }
}
