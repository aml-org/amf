package amf.apicontract.internal.spec.async.emitters.domain

import amf.apicontract.client.scala.model.domain.{Operation, Tag}
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.apicontract.internal.spec.async.AsyncHelper
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeSpecEmitterContext
import amf.apicontract.internal.spec.oas.emitter.domain.{
  OasLikeOperationEmitter,
  OasLikeOperationPartEmitter,
  TagsEmitter
}
import amf.apicontract.internal.spec.spec.OasDefinitions
import org.mulesoft.common.client.lexical.Position
import org.mulesoft.common.client.lexical.Position.ZERO
import amf.core.internal.parser.domain.{FieldEntry, Fields}
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.internal.annotations.OrphanOasExtension
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class AsyncApiOperationEmitter(operation: Operation, ordering: SpecOrdering, isTrait: Boolean = false)(implicit
    spec: OasLikeSpecEmitterContext
) extends OasLikeOperationEmitter(operation, ordering) {

  override def operationPartEmitter(): PartEmitter = AsyncOperationPartEmitter(operation, isTrait, ordering)

}

case class AsyncOperationPartEmitter(operation: Operation, isTrait: Boolean, ordering: SpecOrdering)(
    override implicit val spec: OasLikeSpecEmitterContext
) extends OasLikeOperationPartEmitter(operation, ordering) {

  override def emit(b: PartBuilder): Unit = {
    if (operation.isLink) {
      emitLink(b)
    } else {
      b.obj { eb =>
        val fs         = operation.fields
        val tempResult = mutable.ListBuffer[EntryEmitter]()

        val bindingOrphanAnnotations =
          operation.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))
        fs.entry(OperationModel.Bindings)
          .map(f => tempResult += AsyncApiBindingsEmitter(f.value.value, ordering, bindingOrphanAnnotations))
        fs.entry(OperationModel.Tags)
          .map(f => tempResult += TagsEmitter("tags", f.array.values.asInstanceOf[Seq[Tag]], ordering))
        if (!isTrait) {
          emitMessage(fs).map(reqOrRes => tempResult += reqOrRes)
          fs.entry(OperationModel.Extends).foreach(f => emitTraits(f, tempResult))
        }
        traverse(ordering.sorted(super.commonEmitters ++ tempResult), eb)
      }
    }
  }

  def emitLink(b: PartBuilder): Unit = {
    val label   = operation.linkLabel.option().getOrElse("default")
    val fullRef = OasDefinitions.appendOas3ComponentsPrefix(label, "operationTraits")
    b.obj(_.entry("$ref", fullRef))
  }

  override protected def emitOperationId(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
    fs.entry(OperationModel.OperationId).foreach { f =>
      result += ValueEmitter("operationId", f)
    }
  }

  def emitMessage(fs: Fields): Option[EntryEmitter] = {
    AsyncHelper.messageType(operation.method.value()) match {
      case Some(s) => fs.entry(s.field).map(new AsyncApiMessageEmitter(_, ordering))
      case _       => None
    }
  }

  def emitTraits(f: FieldEntry, tempResult: ListBuffer[EntryEmitter]): Unit = {
    tempResult += AsyncOperationTraitsEmitter(f.arrayValues[Operation], ordering)
  }
}

case class AsyncOperationTraitsDeclarationEmitter(operations: Seq[Operation], ordering: SpecOrdering)(implicit
    spec: OasLikeSpecEmitterContext
) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "operationTraits",
      _.obj(entryBuilder => {
        val entryEmitters = operations.map { op =>
          new AsyncApiOperationEmitter(op, ordering, isTrait = true)
        }
        traverse(ordering.sorted(entryEmitters), entryBuilder)
      })
    )
  }

  override def position(): Position = operations.headOption.map(p => pos(p.annotations)).getOrElse(ZERO)
}

case class AsyncOperationTraitsEmitter(operations: Seq[Operation], ordering: SpecOrdering)(implicit
    spec: OasLikeSpecEmitterContext
) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "traits",
      _.list(partBuilder => {
        val partEmitters = operations.map { op =>
          AsyncOperationPartEmitter(op, isTrait = true, ordering)
        }
        traverse(ordering.sorted(partEmitters), partBuilder)
      })
    )
  }

  override def position(): Position = operations.headOption.map(p => pos(p.annotations)).getOrElse(ZERO)
}
