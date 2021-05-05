package amf.plugins.document.webapi.parser.spec.oas.emitters

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.parser.{Fields, Position}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{
  ApiShapeEmitterContextAdapter,
  ShapeEmitterContext
}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.AnnotationsEmitter
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.metamodel.OperationModel
import amf.plugins.domain.webapi.models.Operation
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
        operationPartEmitter().emit(_)
      )
    )
  }

  def operationPartEmitter(): PartEmitter

  override def position(): Position = pos(operation.annotations)
}

abstract class OasLikeOperationPartEmitter(operation: Operation, ordering: SpecOrdering)(
    implicit val spec: SpecEmitterContext)
    extends PartEmitter {
  protected implicit val shapeCtx: ShapeEmitterContext = ApiShapeEmitterContextAdapter(spec)

  def commonEmitters: Seq[EntryEmitter] = {
    val fs     = operation.fields
    val result = mutable.ListBuffer[EntryEmitter]()
    emitOperationId(fs, result)
    fs.entry(OperationModel.Description).map(f => result += ValueEmitter("description", f))
    fs.entry(OperationModel.Summary).map(f => result += ValueEmitter("summary", f))
    fs.entry(OperationModel.Documentation)
      .map(f =>
        result += OasEntryCreativeWorkEmitter("externalDocs", f.value.value.asInstanceOf[CreativeWork], ordering))
    result ++= AnnotationsEmitter(operation, ordering).emitters
  }

  protected def emitOperationId(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
    fs.entry(OperationModel.Name) match {
      case Some(f) => result += ValueEmitter("operationId", f)
      case None    => fs.entry(OperationModel.OperationId).foreach(f => result += ValueEmitter("operationId", f))
    }
  }

  override def position(): Position = pos(operation.annotations)

}
