package amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations

import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{AmfArray, AmfScalar, RecursiveShape}
import amf.core.parser.{FieldEntry, Value}
import amf.plugins.document.apicontract.contexts.SpecEmitterContext
import amf.plugins.document.apicontract.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.apicontract.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  AgnosticShapeEmitterContextAdapter,
  OasLikeShapeEmitterContextAdapter,
  RamlShapeEmitterContextAdapter,
  ShapeEmitterContext
}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas.OasSchemaEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml.{
  Raml10TypeEmitter,
  RamlRecursiveShapeEmitter
}
import amf.plugins.document.apicontract.vocabulary.VocabularyMappings
import amf.plugins.domain.shapes.models.AnyShape
import amf.validations.RenderSideValidations.RenderValidation

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

  private val fs                                                              = property.fields
  protected override implicit val shapeCtx: OasLikeShapeEmitterContextAdapter = OasLikeShapeEmitterContextAdapter(spec)

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

  protected override implicit val shapeCtx: RamlShapeEmitterContextAdapter = RamlShapeEmitterContextAdapter(spec)
  private val fs                                                           = property.fields
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
