package amf.apicontract.internal.transformation.stages

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document._
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject, AmfScalar, Shape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.adoption.IdAdopter
import amf.core.internal.metamodel.document.ModuleModel
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.unsafe.PlatformSecrets
import amf.shapes.client.scala.model.domain._
import amf.shapes.internal.validation.definitions.ShapeResolutionSideValidations.InvalidTypeExtensionSpecification

import scala.collection.mutable

class TypeExtensionsResolutionStage() extends TransformationStep() with PlatformSecrets {

  type Name          = String
  type WrapperShape  = AnyShape
  type WrappersIndex = mutable.Map[Name, WrapperShape]

  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    implicit val eh: AMFErrorHandler = errorHandler
    implicit val bu: BaseUnit        = model

    model match {
      case declaresModel: DeclaresModel => createShapeWrappers(declaresModel)
      case _                            => // skip
    }
    model
  }

  /** Type extensions in GraphQL are allOf & oneOf relationships. We need to collect each extension (and the original
    * type) and create a "wrapper" shape that contains every extension in "AnyShapeModel.and" or "AnyShapeModel.or"
    * fields
    * @param declaresModel
    *   base unit that declares types
    * @param eh
    *   error handler
    * @param model
    *   just a base unit node to report errors
    */
  private def createShapeWrappers(declaresModel: DeclaresModel)(implicit eh: AMFErrorHandler, model: BaseUnit): Unit = {
    val shapesIndex = indexShapesByName(declaresModel).retain((_, values) =>
      values.size > 1
    ) // we wrap only when we have 1+ shapes with same name

    implicit val wrapperShapesIndex: WrappersIndex = shapesIndex
      .map { case (name, shapesWithSameName) =>
        // Validate
        validateThat(shapesWithSameName).containAtMostOneNonExtensionShape
        validateThat(shapesWithSameName).areAllOfSameType

        // Create wrapper
        val wrapper = aggregateIntoWrapperShape(shapesWithSameName)

        name -> wrapper
      }

    // Replace shapes with their wrappers in declarations
    val wrapped  = shapesIndex.values.flatten.toSeq
    val wrappers = wrapperShapesIndex.values.toSeq

    setDeclares(declaresModel, remove = wrapped, add = wrappers)

    // Set correct IDs
    new IdAdopter(declaresModel, declaresModel.id).adoptFromRoot()

    // Redirect all references to the new wrapper shapes
    redirectReferencesToWrappersIn(model)
  }

  private def setDeclares(model: DeclaresModel, remove: Seq[AnyShape], add: Seq[AnyShape]): Unit = {
    val newDeclares = model.declares.diff(remove) ++ add

    // Keep annotations
    val annotations = model.fields
      .entry(ModuleModel.Declares)
      .map(_.value.annotations)
      .getOrElse(Annotations())

    model.setArrayWithoutId(ModuleModel.Declares ,newDeclares, annotations)
  }

  private def indexShapesByName(model: DeclaresModel): mutable.Map[Name, Seq[AnyShape]] = {
    val index = mutable.Map.empty[Name, Seq[AnyShape]]
    model.declares.foreach {
      case shape: AnyShape =>
        val shapeName = shape.name.value()
        index.get(shapeName) match {
          case Some(others) => index(shapeName) = others :+ shape
          case None         => index(shapeName) = Seq(shape)
        }
      case _ => // ignore
    }
    index
  }

  /** When we have references to a shape that is extended, we will need to redirect these references to the newly
    * created wrapper shape
    * @param model
    *   model in which we will redirect references
    * @param index
    *   index name -> wrapper shape redirection target
    */
  private def redirectReferencesToWrappersIn(model: BaseUnit)(implicit index: WrappersIndex): Unit = {
    def isWrapper(obj: AmfObject): Boolean = {
      obj match {
        case anyShape: AnyShape =>
          anyShape.name
            .option()
            .flatMap(name => index.get(name))
            .contains(anyShape)
        case _ => false
      }
    }

    model.iterator().foreach {
      case obj: AmfObject if !isWrapper(obj) => // skip wrappers
        obj.fields
          .fields()
          .foreach { entry =>
            entry.value.value match {
              // only shapes can be redirected
              case shape: Shape =>
                val fieldValue = redirectReferenceTo(shape).getOrElse(shape)
                obj.setWithoutId(entry.field, fieldValue, entry.value.annotations)

              // if we have an array of shapes as value, iterate it
              case arr: AmfArray =>
                val fieldValue = arr.values.map { elem => redirectReferenceTo(elem).getOrElse(elem) }
                obj.setArrayWithoutId(entry.field, fieldValue, entry.value.annotations)

              case _ => // skip
            }
          }
      case _ => // skip
    }
  }

  private def redirectReferenceTo(element: AmfElement)(implicit index: WrappersIndex): Option[WrapperShape] = {
    element match {
      case anyShape: AnyShape =>
        index
          .get(anyShape.name.value())
          .filter(ws => ws != anyShape) // anyShape is not already the wrapper shape
      case _ => None
    }
  }

  /** We aggregate shapes extensions into a wrapper shape. Each shape extension has different behaviors. The behavior
    * mapping is the following:
    *
    * Objects, Input objects, Interfaces, Scalar => AllOf
    *
    * Union, Enum => OneOf
    *
    * @param shapesWithSameName
    *   shapes to be aggregated into a wrapper shape
    * @tparam T
    *   to make sure that every extension is of the same type
    * @return
    */
  private def aggregateIntoWrapperShape[T <: AnyShape](shapesWithSameName: Seq[T]): AnyShape = {

    /** We need to match on the `head` because T is lost by erasure and class tags don't work on higher kinded types. In
      * other words, we cannot obtain a class tag for T from a Seq[T] argument
      */
    val head = shapesWithSameName.head

    head match {
      case _: ScalarShape if head.values.nonEmpty => // enum
        val these = shapesWithSameName.asInstanceOf[Seq[ScalarShape]]
        CreateWrapper.oneOf(these)

      case _: UnionShape =>
        val these = shapesWithSameName.asInstanceOf[Seq[UnionShape]]
        CreateWrapper.oneOf(these)

      case _: ScalarShape =>
        val these = shapesWithSameName.asInstanceOf[Seq[ScalarShape]]
        CreateWrapper.allOf(these)

      case _: NodeShape => // object, input object, interface
        val these = shapesWithSameName.asInstanceOf[Seq[NodeShape]]
        CreateWrapper.allOf(these)
    }
  }
}

//noinspection UnitMethodIsParameterless
sealed case class validateThat(shapesWithSameName: Seq[AnyShape])(implicit eh: AMFErrorHandler, model: BaseUnit) {

  /** The original shape and each extension will share the same `name`, but there can only be at most 1 non-extension
    * shape and N extensions
    */
  def containAtMostOneNonExtensionShape: Unit = {
    val nonExtensionShapesCount = shapesWithSameName.count(!_.isExtension.value())
    if (nonExtensionShapesCount > 1) {
      val message = s"Duplicate type declarations with name ${shapesWithSameName.head.name.value()}"
      eh.violation(InvalidTypeExtensionSpecification, model, message, model.annotations)
    }
  }

  /** All shapes must be of the same type (e.g. NodeShape, ScalarShape, etc.)
    */
  def areAllOfSameType: Unit = {

    val getGraphqlType = (s: AnyShape) => {
      s match {
        case n: NodeShape if n.isAbstract.value()  => "INTERFACE"
        case n: NodeShape if n.isInputOnly.value() => "INPUT_OBJECT"
        case _: NodeShape                          => "OBJECT"
        case s: ScalarShape if s.values.nonEmpty   => "ENUM"
        case _: ScalarShape                        => "SCALAR"
        case _: UnionShape                         => "UNION"
      }
    }

    val distinctTypes = shapesWithSameName
      .map(getGraphqlType)
      .toSet

    if (distinctTypes.size > 1) {
      val message =
        s"Type extensions with same ${shapesWithSameName.head.name.value()} must be of same type. Found ${distinctTypes.mkString(",")}"
      eh.violation(InvalidTypeExtensionSpecification, model, message, model.annotations)
    }
  }

}

object CreateWrapper {
  def allOf(shapesWithSameName: Seq[NodeShape]): NodeShape = {
    val prototype = shapesWithSameName.head // we use the first shape as a "kind of" prototype
    NodeShape()
      .withAnd(shapesWithSameName)
      .withName(prototype.name.value())
      .withIsInputOnly(prototype.isInputOnly.value()) // input objects
      .withIsAbstract(prototype.isAbstract.value())   // interfaces
  }

  def allOf(shapesWithSameName: Seq[ScalarShape]): ScalarShape = {
    val prototype = shapesWithSameName.head // we use the first shape as a "kind of" prototype
    ScalarShape()
      .withAnd(shapesWithSameName)
      .withName(prototype.name.value())
      .withDataType(prototype.dataType.value())
  }

  def oneOf(shapesWithSameName: Seq[UnionShape]): UnionShape = {
    val prototype = shapesWithSameName.head // we use the first shape as a "kind of" prototype
    UnionShape()
      .withOr(shapesWithSameName)
      .withName(prototype.name.value())
  }

  def oneOf(shapesWithSameName: Seq[ScalarShape]): ScalarShape = {
    val prototype = shapesWithSameName.head // we use the first shape as a "kind of" prototype
    ScalarShape()
      .withOr(shapesWithSameName)
      .withName(prototype.name.value())
      .withDataType(prototype.dataType.value())
  }

}
