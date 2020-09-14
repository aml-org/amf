package amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations

import amf.core.emitter.BaseEmitters.{ArrayEmitter, ValueEmitter}
import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.parser.{FieldEntry, Value}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.vocabulary.VocabularyMappings

import scala.collection.mutable.ListBuffer

abstract class AnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext) {

  private val fs = property.fields
  protected val shapeEmitters: Seq[Emitter]

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

          result += ArrayEmitter("allowedTargets", finalFieldEntry, ordering)
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
