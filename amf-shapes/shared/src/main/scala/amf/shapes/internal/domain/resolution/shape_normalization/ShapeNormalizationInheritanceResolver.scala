package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain._
import amf.core.internal.annotations._
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.validation.CoreValidations.{RecursiveShapeSpecification, TransformationValidation}
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.metamodel._
import amf.shapes.internal.validation.definitions.ShapeResolutionSideValidations.InvalidTypeInheritanceWarningSpecification

import scala.collection.mutable

case class ShapeNormalizationInheritanceResolver(context: NormalizationContext) {

  private val algorithm: MinShapeAlgorithm = new MinShapeAlgorithm()(this)
  private var queue: Seq[Shape]            = Seq.empty

  def log(msg: String): Unit                 = context.logger.log(msg)
  def getCached(shape: Shape): Option[Shape] = context.resolvedInheritanceIndex.get(shape.id)

  def remove(shape: Shape): Unit = context.resolvedInheritanceIndex -= shape.id

  def minShape(derivedShape: Shape, superShape: Shape): Shape = {
    log(s"minShape: ${derivedShape.debugInfo()} => ${superShape.debugInfo()}")
    try {
      val r = algorithm.computeMinShape(derivedShape, superShape)
      log(s"minShape returning: ${r.debugInfo()}")
      r
    } catch {
      case e: InheritanceIncompatibleShapeError =>
        context.errorHandler.violation(
          InvalidTypeInheritanceWarningSpecification,
          derivedShape.id,
          e.property.orElse(Some(ShapeModel.Inherits.value.iri())),
          e.getMessage,
          e.position,
          e.location
        )
        derivedShape
      case other: Throwable =>
        context.errorHandler.violation(
          TransformationValidation,
          derivedShape.id,
          Some(ShapeModel.Inherits.value.iri()),
          Option(other.getMessage()).getOrElse(other.toString),
          derivedShape.position(),
          derivedShape.location()
        )
        derivedShape
    }
  }

  private val currentInheritancePath = mutable.ArrayBuffer[Shape]()

  // This variable is used to back track
  private var detectedRecursion = false

  // ID of the shape where inheritance recursion was detected
  private var recursionGenerator = "fakeId"

  private def addToCache(shape: Shape, id: String) = context.resolvedInheritanceIndex += (shape, id)
  private def addToCache(shape: Shape)             = context.resolvedInheritanceIndex += shape

  def removeFromQueue(shape: Shape): Unit = {
    log(s"removing from queue: ${shape.debugInfo()}")
    queue = queue.filterNot(_ == shape)
  }
  def queue(shape: Shape): Unit = {
    log(s"queueing: ${shape.debugInfo()}")
    queue = queue :+ shape
  }

  def normalize(shape: Shape, skipQueue: Boolean = false): Shape = {
    log(s"normalize: ${shape.debugInfo()}")
    getCached(shape) match {
      case Some(resolvedInheritance) =>
        log(s"normalize returning cached: ${shape.debugInfo()}")
        resolvedInheritance
      case _ =>
        val result = normalizeAction(shape)
        addToCache(result)

        while (queue.nonEmpty && !skipQueue) {
          val next = queue.head
          log(s"queue is not empty ----- ")
          queue = queue.tail
          normalize(next, skipQueue = true) // do not nest queued normalizations
        }

        result
    }
  }

  private def normalizeAction(shape: Shape): Shape = {
    log(s"normalizeAction: ${shape.debugInfo()}")
    if (isPartOfInheritanceCycle(shape)) {
      log(s"detected cycle on: ${shape.debugInfo()}")
      invalidRecursionError(shape)
      markInheritanceRecursionDetected(shape)
      shape
    } else if (hasSuperTypes(shape)) {
      log(s"has super types: ${shape.debugInfo()}")
      currentInheritancePath += shape
      val resolvedShape = resolveInheritance(shape)
      currentInheritancePath.remove(currentInheritancePath.size - 1)
      resolvedShape
    } else {
      log(s"normalizeAction (no action) returning: ${shape.debugInfo()}")
      shape
    }
  }

  private def markInheritanceRecursionDetected(shape: Shape): Unit = {
    detectedRecursion = true
    recursionGenerator = shape.id
  }

  private def resolveInheritance(shape: Shape): Shape = {
    log(s"resolveInheritance: ${shape.debugInfo()}")
    if (canReplaceForInherits(shape)) {
      log(s"replacing for parent: ${shape.debugInfo()} => ${shape.inherits.head.debugInfo()}")
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

      resolvedShape
    }
  }

  private def inheritFromSuperTypes(shape: Shape, superTypes: Seq[Shape]) = {
    shape.fields.removeField(ShapeModel.Inherits)
    superTypes.fold(shape) { (accShape, superType) =>
      // go up the inheritance chain before applying type. We want to apply inheritance with the accumulated super type
      log(s"inherit from super type: ${superType.debugInfo()}")
      context.logger.addPadding()
      val normalizedSuperType = normalize(superType, skipQueue = true)
      context.logger.removePadding()
      if (detectedRecursion) accShape
      else {

        /** We need to call the AnyShapeAdjuster because types in RT/Traits that inherit from declared types are
          * AnyShapes. When an AnyShape inherits from a NodeShape the min shape algorithm completely messes up the
          * inheritance computation. TODO: try to fix this in minShape rather than here
          */
        val r = minShape(AnyShapeAdjuster(accShape), normalizedSuperType)
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
      typeIsCompatibleWithParent(shape, superType) && allFieldsMatchSuperTypes(shape, superType, ignoredFields)
    case _ => false
  }

  private def typeIsCompatibleWithParent(child: Shape, parent: Shape): Boolean = {
    (child, parent) match {
      case (c: AnyShape, _) if c.isStrictAnyMeta => true             // child type might not be defined yet
      case (_: ArrayShape, _: MatrixShape)       => true
      case (_: MatrixShape, _: ArrayShape)       => true
      case (c, p)                                => c.meta == p.meta // are exactly same type
    }
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

  private def isPartOfInheritanceCycle(shape: Shape) = {
    val shapeIsDeclared = shape.annotations.contains(classOf[DeclaredElement])
    currentInheritancePath.find(_.id == shape.id).exists { inPath =>
      val inPathIsDeclared = inPath.annotations.contains(classOf[DeclaredElement])
      (inPath != shape && !shapeIsDeclared) || (shape == inPath && inPathIsDeclared && shapeIsDeclared)
    }
  }

  private def invalidRecursionError(lastVersion: Shape): Unit = {
    val chain = currentInheritancePath.map(_.name.value()).mkString(" -> ") + s" -> ${lastVersion.name.value()}"
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
