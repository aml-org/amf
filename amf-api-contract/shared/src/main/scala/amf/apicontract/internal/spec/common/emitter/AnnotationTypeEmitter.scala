package amf.apicontract.internal.spec.common.emitter

import amf.apicontract.internal.spec.raml
import amf.apicontract.internal.spec.oas.emitter
import amf.apicontract.internal.spec.oas.emitter.context
import amf.apicontract.internal.spec.oas.emitter.context.{OasLikeShapeEmitterContextAdapter, OasSpecEmitterContext}
import amf.apicontract.internal.spec.raml.emitter.RamlShapeEmitterContextAdapter
import amf.apicontract.internal.spec.raml.emitter.context.RamlSpecEmitterContext
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, RecursiveShape}
import amf.core.internal.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.internal.parser.domain.{FieldEntry, Value}
import amf.core.internal.render.BaseEmitters.ValueEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter, PartEmitter}
import amf.shapes.client.scala.domain.models.AnyShape
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import amf.shapes.internal.spec.oas.emitter.OasSchemaEmitter
import amf.shapes.internal.spec.raml.emitter.{Raml10TypeEmitter, RamlRecursiveShapeEmitter}
import amf.shapes.internal.validation.definitions.RenderSideValidations.RenderValidation
import amf.shapes.internal.vocabulary.VocabularyMappings

import scala.collection.mutable.ListBuffer

abstract class AnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext) {

  private val fs = property.fields
  protected val shapeEmitters: Seq[Emitter]
  protected implicit val shapeCtx: ShapeEmitterContext

  def emitters(): Either[Seq[EntryEmitter], PartEmitter] = {

    shapeEmitters.toList match {
      case (head: EntryEmitter) :: tail =>
        val result = ListBuffer[EntryEmitter]()
        fs.entry(CustomDomainPropertyModel.DisplayName).map(f => result += ValueEmitter("displayName", f))

        fs.entry(CustomDomainPropertyModel.Description).map(f => result += ValueEmitter("description", f))

        fs.entry(CustomDomainPropertyModel.Domain).map { f =>
          val scalars = f.array.scalars.map { s =>
            VocabularyMappings.uriToRaml.get(s.toString) match {
              case Some(identifier) => AmfScalar(identifier, s.annotations)
              case None             => s
            }
          }
          val finalArray      = AmfArray(scalars, f.array.annotations)
          val finalFieldEntry = FieldEntry(f.field, Value(finalArray, f.value.annotations))

          result += spec.arrayEmitter("allowedTargets", finalFieldEntry, ordering)
        }

        result ++= shapeEmitters.map(_.asInstanceOf[EntryEmitter])

        result ++= AnnotationsEmitter(property, ordering).emitters
        Left(result)
      case (head: PartEmitter) :: Nil => Right(head)
      case Nil                        => Left(Nil)
      case other =>
        throw new Exception(s"IllegalTypeDeclarations found: $other") // todo handle
    }
  }
}

case class OasAnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering)(
    implicit spec: OasSpecEmitterContext)
    extends AnnotationTypeEmitter(property, ordering) {

  private val fs = property.fields
  protected override implicit val shapeCtx: OasLikeShapeEmitterContextAdapter =
    context.OasLikeShapeEmitterContextAdapter(spec)

  override protected val shapeEmitters: Seq[Emitter] = fs
    .entry(CustomDomainPropertyModel.Schema)
    .map({ f =>
      OasSchemaEmitter(f, ordering, Nil)
    })
    .toSeq
}

case class RamlAnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering)(
    implicit spec: RamlSpecEmitterContext)
    extends AnnotationTypeEmitter(property, ordering) {

  protected override implicit val shapeCtx: RamlShapeEmitterContextAdapter =
    raml.emitter.RamlShapeEmitterContextAdapter(spec)
  private val fs = property.fields
  override protected val shapeEmitters: Seq[Emitter] = fs
    .entry(CustomDomainPropertyModel.Schema)
    .map({ f =>
      // we merge in the main body
      Option(f.value.value) match {
        case Some(shape: AnyShape) =>
          Raml10TypeEmitter(shape, ordering, Nil, Nil).emitters() match {
            case es if es.forall(_.isInstanceOf[EntryEmitter]) => es.collect { case e: EntryEmitter => e }
            case other                                         => throw new Exception(s"IllegalTypeDeclarations found: $other")
          }
        case Some(shape: RecursiveShape) => RamlRecursiveShapeEmitter(shape, ordering, Nil).emitters()
        case Some(x) =>
          spec.eh.violation(RenderValidation,
                            property.id,
                            None,
                            "Cannot emit raml type for a shape that is not an AnyShape",
                            x.position(),
                            x.location())
          Nil
        case _ => Nil // ignore
      }
    }) match {
    case Some(emitters) => emitters
    case _              => Nil
  }
}
