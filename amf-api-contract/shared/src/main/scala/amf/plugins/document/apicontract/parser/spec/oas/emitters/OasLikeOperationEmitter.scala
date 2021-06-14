package amf.plugins.document.apicontract.parser.spec.oas.emitters

import amf.core.client.common.position.Position
import amf.core.internal.parser.domain.Fields
import amf.core.internal.render.BaseEmitters._
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.plugins.document.apicontract.contexts.SpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration._
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.AnnotationsEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  AgnosticShapeEmitterContextAdapter,
  ShapeEmitterContext
}
import amf.plugins.domain.apicontract.metamodel.OperationModel
import amf.plugins.domain.apicontract.models.Operation
import amf.plugins.domain.shapes.models.CreativeWork
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
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

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
