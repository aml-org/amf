package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.validation.CoreValidations.{RecursiveShapeSpecification, TransformationValidation}
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.domain.metamodel._

import scala.collection.mutable.ListBuffer

case class ShapeReferencesUpdater()(implicit val context: NormalizationContext) {
  private val recursionAnalyzer = new RecursionAnalyzer(context.errorHandler)

  def update(shape: Shape): Shape = {
    val latestVersion = retrieveLatestVersionOf(shape)
    if (recursionAnalyzer.recursionDetected(latestVersion)) handleRecursion(latestVersion) else updateReferences(latestVersion)
  }

  private def updateReferences(shape: Shape): Shape =
    recursionAnalyzer.executeWhileDetectingRecursion(shape, updateReferencesIn)

  private def handleRecursion(shape: Shape): RecursiveShape = {
    recursionAnalyzer.detectRecursionsFromUnions(shape)
    if (recursionAnalyzer.isInvalidRecursion(shape)) reportInvalidRecursionError(shape)
    RecursiveShape(shape).withSupportsRecursion(true)
  }

  private def retrieveLatestVersionOf(shape: Shape) = {
    context.resolvedInheritanceCache.get(shape.id) match {
      case Some(resolvedInheritance) => resolvedInheritance
      case _                         => shape
    }
  }

  private def updateReferencesIn(shape: Shape) = {
    UnnecessaryAnnotationsRemover(shape) // Shouldn't be here
    ExplicitFieldAnnotationSetter(shape) // Shouldn't be here

    shape match {
      case union: UnionShape         => updateReferencesInUnion(union)
      case scalar: ScalarShape       => updateReferencesInScalar(scalar)
      case array: ArrayShape         => updateReferencesInArray(array)
      case matrix: MatrixShape       => updateReferencesInMatrix(matrix)
      case tuple: TupleShape         => updateReferencesInTuple(tuple)
      case property: PropertyShape   => updateReferencesInProperty(property)
      case fileShape: FileShape      => updateReferencesInShape(fileShape)
      case nil: NilShape             => updateReferencesInShape(nil)
      case node: NodeShape           => updateReferencesInNode(node)
      case any: AnyShape             => updateReferencesInAny(any)
      case recursive: RecursiveShape => recursive
    }
  }

  private def updateReferencesInShape(any: Shape) = {
    updateReferencesInLogicalConstraints(any)
    any
  }

  private def updateReferencesInAny(any: AnyShape) = {
    val adjusted = AnyShapeAdjuster(any) // Analyze if it's possible to remove this case
    updateReferencesInLogicalConstraints(adjusted)
    adjusted
  }

   private def updateReferencesInNode(node: NodeShape): Shape = {
    updateReferencesInLogicalConstraints(node)
    updateReferencesInProperties(node)
    updateReferencesInAdditionalPropertiesSchema(node)
    node
  }

   private def updateReferencesInProperty(property: PropertyShape): Shape = {
    val updatedRange = update(property.range)
    val updatedProperty = updatedRange match {
      case _: RecursiveShape =>
        // If id is not changed, JSONLD will only render one version of this property (may exists one with Recursive shape as range and the original)
        // Cannot clone because property is recursive
        property.copyShape().withId(property.id + "/recursiveProp")
      case _ => property
    }

    updatedProperty.fields.setWithoutId(
      PropertyShapeModel.Range,
      updatedRange,
      property.fields.getValue(PropertyShapeModel.Range).annotations
    )

    updatedProperty
  }

   private def updateReferencesInScalar(scalar: ScalarShape): Shape = {
    updateReferencesInLogicalConstraints(scalar)
    scalar
  }

   private def updateReferencesInArray(array: ArrayShape): Shape = {
    updateReferencesInLogicalConstraints(array)
    array.items match {
      case items: Shape =>
        val newItems = normalizeAllowingRecursionIn(items, canBeEmpty(array))
        array.fields.setWithoutId(ArrayShapeModel.Items, newItems)
        array
      case _ => array
    }
  }

   private def updateReferencesInMatrix(matrix: MatrixShape): Shape = {
    updateReferencesInLogicalConstraints(matrix)
    matrix.items match {
      case items: Shape =>
        val newItems = update(items)
        matrix.fields.setWithoutId(ArrayShapeModel.Items, newItems)
        matrix
      case _ => matrix
    }
  }

   private def updateReferencesInTuple(tuple: TupleShape): Shape = {
    updateReferencesInLogicalConstraints(tuple)
    val newItems = tuple.items.map(shape => update(shape))
    tuple.fields.setWithoutId(
      TupleShapeModel.TupleItems,
      AmfArray(newItems),
      Option(tuple.fields.getValue(TupleShapeModel.TupleItems)).map(_.annotations).getOrElse(Annotations())
    )
    tuple
  }

   private def updateReferencesInUnion(union: UnionShape): Shape = {
    val updatedAnyOf = updateReferencesInUnionMembers(union)
    val anyOfAnnotations =
      union.fields.getValueAsOption(UnionShapeModel.AnyOf).map(_.annotations).getOrElse(Annotations())
    union.fields.setWithoutId(UnionShapeModel.AnyOf, AmfArray(updatedAnyOf), anyOfAnnotations)
    union

  }

   private def updateReferencesInLogicalConstraints(shape: Shape): Unit = {
    updateReferencesInLogicalConstraint(shape, ShapeModel.And)
    updateReferencesInLogicalConstraint(shape, ShapeModel.Or)
    updateReferencesInLogicalConstraint(shape, ShapeModel.Xone)
    updateReferencesInLogicalConstraint(shape, ShapeModel.Not)
  }

   private def updateReferencesInLogicalConstraint(shape: Shape, constraintField: Field): Unit = {
    shape.fields.getValueAsOption(constraintField) match {
      case Some(constraint) =>
        constraint.value match {
          case array: AmfArray =>
            val updatedLogicalConstraint = array.values.map(c => update(c.asInstanceOf[Shape]))
            shape.setArrayWithoutId(constraintField, updatedLogicalConstraint, constraint.annotations)
          case s: Shape =>
            shape.setWithoutId(constraintField, update(s), constraint.annotations)
          case _ =>
        }
      case _ =>
    }
  }

  private def updateReferencesInProperties(node: NodeShape): Unit = {
    val updatedProperties = node.properties.map { prop =>
      normalizeAllowingRecursionIn(prop, isOptionalProperty(prop))
    }
    node.setArrayWithoutId(NodeShapeModel.Properties, updatedProperties)
  }

  private def updateReferencesInAdditionalPropertiesSchema(node: NodeShape): Unit = {
    Option(node.additionalPropertiesSchema).foreach { schema =>
      val updated = normalizeAllowingRecursionIn(schema)
      node.setWithoutId(NodeShapeModel.AdditionalPropertiesSchema, updated)
    }
  }

  private def updateReferencesInUnionMembers(union: UnionShape) = {
    recursionAnalyzer.executeAndAnalyzeRecursionInUnion(
      union, { union =>
        val flattenedAnyOf: ListBuffer[Shape] = ListBuffer()

        union.anyOf.foreach { unionMember: Shape =>
          recursionAnalyzer.traversedUnionMembers.append(unionMember.id)
          val updatedUnionMember = update(unionMember)
          recursionAnalyzer.traversedUnionMembers.remove(recursionAnalyzer.traversedUnionMembers.size - 1)
          updatedUnionMember match {
            case nestedUnion: UnionShape => nestedUnion.anyOf.foreach(member => flattenedAnyOf += member)
            case other: Shape            => flattenedAnyOf += other
          }
        }
        flattenedAnyOf
      }
    )
  }

  private def normalizeAllowingRecursionIn(shape: Shape, condition: Boolean = true) =
    recursionAnalyzer.executeAllowingRecursionIn(shape, update, condition)

  private def isOptionalProperty(prop: PropertyShape) = prop.minCount.option().forall(_ == 0)

  private def canBeEmpty(array: ArrayShape) = array.minItems.option().forall(_ == 0)

  private def reportInvalidRecursionError(latestVersion: Shape): Unit = {
    context.errorHandler.violation(
      RecursiveShapeSpecification,
      latestVersion.id,
      None,
      "Error recursive shape",
      latestVersion.position(),
      latestVersion.location()
    )
  }
}
