package amf.plugins.document.webapi.parser.spec.oas.emitters

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.parser.{FieldEntry, Fields, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.{Oas3SpecEmitterFactory, OasSpecEmitterContext}
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain.{
  OasCallbacksEmitter,
  OasWithExtensionsSecurityRequirementsEmitter
}
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import amf.plugins.domain.webapi.metamodel.OperationModel
import amf.plugins.domain.webapi.models.{Callback, Operation, Tag}
import org.yaml.model.YDocument

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class OasLikeOperationEmitter(operation: Operation, ordering: SpecOrdering)(
    implicit val spec: SpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val fs = operation.fields

    sourceOr(
      operation.annotations,
      b.complexEntry(
        ScalarEmitter(fs.entry(OperationModel.Method).get.scalar).emit(_),
        _.obj { b =>
          val result = mutable.ListBuffer[EntryEmitter]()

          fs.entry(OperationModel.Name).map(f => result += ValueEmitter("operationId", f))
          fs.entry(OperationModel.Description).map(f => result += ValueEmitter("description", f))
          fs.entry(OperationModel.Summary).map(f => result += ValueEmitter("summary", f))
          fs.entry(OperationModel.Documentation)
            .map(
              f =>
                result += OasEntryCreativeWorkEmitter("externalDocs",
                                                      f.value.value.asInstanceOf[CreativeWork],
                                                      ordering))
          result ++= AnnotationsEmitter(operation, ordering).emitters

          emitSpecific(fs, result)

          traverse(ordering.sorted(result), b)
        }
      )
    )
  }

  def emitSpecific(fs: Fields, tempResult: ListBuffer[EntryEmitter]): ListBuffer[EntryEmitter]

  override def position(): Position = pos(operation.annotations)
}
