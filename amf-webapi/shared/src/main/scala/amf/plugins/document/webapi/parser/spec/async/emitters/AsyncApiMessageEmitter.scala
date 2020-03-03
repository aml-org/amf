package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.emitter.BaseEmitters._
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.domain.{AmfScalar, Shape}
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.async.parser.AsyncSchemaFormats
import amf.plugins.document.webapi.parser.spec.declaration.AsyncSchemaEmitter
import amf.plugins.document.webapi.parser.spec.domain.MultipleExampleEmitter
import amf.plugins.document.webapi.parser.spec.oas.emitters.TagsEmitter
import amf.plugins.domain.shapes.models.{CreativeWork, Example}
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import amf.plugins.domain.webapi.metamodel.{MessageModel, PayloadModel}
import amf.plugins.domain.webapi.models._
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiMessageEmitter(fieldEntry: FieldEntry, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    val messages = fieldEntry.arrayValues[Message]
    messages.size match {
      case 1          => emitSingle(b, messages.head)
      case s if s > 1 => emitMultiple(b)
      case _          => Unit
    }
  }

  private def emitSingle(b: YDocument.EntryBuilder, message: Message): Unit = {
    val emitter = new AsyncApiMessageContentEmitter(message, ordering)
    b.complexEntry(
      ScalarEmitter(AmfScalar("message")).emit,
      emitter.emit
    )
  }

  private def emitMultiple(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      YNode("message"),
      _.obj(new AsyncApiOneOfMessageEmitter(fieldEntry, ordering).emit)
    )
  }

  override def position(): Position = pos(fieldEntry.value.annotations)
}

private class AsyncApiOneOfMessageEmitter(fieldEntry: FieldEntry, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    val messages: Seq[Message] = fieldEntry.arrayValues[Message]
    val emitters               = messages.map(x => new AsyncApiMessageContentEmitter(x, ordering))
    b.entry(
      YNode("oneOf"),
      _.list(traverse(ordering.sorted(emitters), _))
    )
  }

  override def position(): Position = pos(fieldEntry.value.annotations)
}

private class AsyncApiMessageContentEmitter(message: Message, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends PartEmitter {

  override def emit(b: YDocument.PartBuilder): Unit = {
    val fs = message.fields
    sourceOr(
      message.annotations,
      b.obj { emitter =>
        {
          val result = ListBuffer[EntryEmitter]()
          val bindingOrphanAnnotations =
            message.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))
          fs.entry(MessageModel.Headers).foreach(emitHeader(result, _))
          fs.entry(MessageModel.Payloads).foreach(f => emitPayloads(f, result))
          fs.entry(MessageModel.CorrelationId)
            .map(f => result += new AsyncApiCorrelationIdEmitter(f.element.asInstanceOf[CorrelationId], ordering))
          fs.entry(MessageModel.Name).map(f => result += ValueEmitter("name", f))
          fs.entry(MessageModel.Title).map(f => result += ValueEmitter("title", f))
          fs.entry(MessageModel.Summary).map(f => result += ValueEmitter("summary", f))
          fs.entry(MessageModel.Description).map(f => result += ValueEmitter("description", f))
          fs.entry(MessageModel.Tags)
            .map(f => result += TagsEmitter("tags", f.array.values.asInstanceOf[Seq[Tag]], ordering))
          fs.entry(MessageModel.Documentation)
            .map(f => result += new AsyncApiCreativeWorksEmitter(f.element.asInstanceOf[CreativeWork], ordering))
          fs.entry(MessageModel.Bindings)
            .foreach(f => result += new AsyncApiBindingsEmitter(f, ordering, bindingOrphanAnnotations))
          fs.entry(MessageModel.Examples)
            .foreach(f => result += MultipleExampleEmitter("examples", f.arrayValues[Example], ordering, Seq())) // TODO: references

          traverse(ordering.sorted(result), emitter)
        }
      }
    )
  }

  private def emitHeader(result: ListBuffer[EntryEmitter], field: FieldEntry): Unit = {
    field.arrayValues[Parameter].headOption.foreach { param =>
      val toEmit = param.schema
      result += AsyncSchemaEmitter("headers", toEmit, ordering, Seq())
    }
  }

  private def emitPayloads(f: FieldEntry, result: ListBuffer[EntryEmitter]): Unit = {
    f.arrayValues[Payload].headOption.foreach { payload =>
      val fs              = payload.fields
      val schemaMediaType = payload.schemaMediaType.option()
      fs.entry(PayloadModel.MediaType).map(field => result += ValueEmitter("contentType", field))
      fs.entry(PayloadModel.SchemaMediaType).map(field => result += ValueEmitter("schemaFormat", field))
      fs.entry(PayloadModel.Schema)
        .map(
          field =>
            result += AsyncSchemaEmitter("payload",
                                         field.element.asInstanceOf[Shape],
                                         ordering,
                                         List(),
                                         schemaMediaType))
    }
  }

  override def position(): Position = pos(message.annotations)
}
