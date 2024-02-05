package amf.apicontract.internal.spec.async.emitters.domain

import amf.apicontract.client.scala.model.domain.Server
import amf.apicontract.internal.metamodel.domain.ServerModel
import amf.apicontract.internal.spec.common.emitter.{
  AgnosticShapeEmitterContextAdapter,
  OasServerVariablesEmitter,
  SecurityRequirementsEmitter
}
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeSpecEmitterContext
import amf.apicontract.internal.spec.oas.emitter.domain.OasTagToReferenceEmitter
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.internal.annotations.OrphanOasExtension
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import org.mulesoft.common.client.lexical.Position
import org.mulesoft.common.client.lexical.Position.ZERO
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YDocument, YNode}

import scala.collection.mutable.ListBuffer

class AsyncApiServersEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  val key = "servers"

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val serverEmitters =
      f.array.values.map(x => x.asInstanceOf[Server]).map(new AsyncApiSingleServerEmitter(_, ordering))
    b.entry(
      key,
      _.obj(b => serverEmitters.map(e => e.emit(b)))
    )
  }

  override def position(): Position = pos(f.element.annotations)
}

class AsyncApiSingleServerEmitter(server: Server, ordering: SpecOrdering)(implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    val serverName = server.name.value()
    b.entry(YNode(serverName), new AsyncApiServerPartEmitter(server, ordering).emit(_))
  }

  override def position(): Position = pos(server.annotations)
}

class AsyncApiServerPartEmitter(server: Server, ordering: SpecOrdering)(implicit val spec: OasLikeSpecEmitterContext)
    extends PartEmitter {
  protected implicit val shapeCtx = AgnosticShapeEmitterContextAdapter(spec)
  override def emit(b: YDocument.PartBuilder): Unit = {

    if (server.isLink) {
      emitLink(b)
    } else {
      val result = ListBuffer[EntryEmitter]()
      val fs     = server.fields
      val bindingOrphanAnnotations =
        server.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))

      fs.entry(ServerModel.Url).foreach(f => result += ValueEmitter("url", f))
      fs.entry(ServerModel.Protocol).foreach(f => result += ValueEmitter("protocol", f))
      fs.entry(ServerModel.ProtocolVersion).foreach(f => result += ValueEmitter("protocolVersion", f))
      fs.entry(ServerModel.Description).foreach(f => result += ValueEmitter("description", f))
      fs.entry(ServerModel.Variables).foreach(f => result += OasServerVariablesEmitter(f, ordering))
      fs.entry(ServerModel.Security).foreach(f => result += SecurityRequirementsEmitter("security", f, ordering))
      fs.entry(ServerModel.Bindings)
        .foreach(f => result += AsyncApiBindingsEmitter(f.value.value, ordering, bindingOrphanAnnotations))

      result ++= AnnotationsEmitter(server, ordering).emitters

      b.obj(traverse(ordering.sorted(result), _))
    }

  }

  def emitLink(b: PartBuilder): Unit = {
    OasTagToReferenceEmitter(server).emit(b)
  }

  override def position(): Position = pos(server.annotations)
}

case class AsyncApiServersDeclarationEmitter(key: String, servers: Seq[Server], ordering: SpecOrdering)(implicit
    val spec: OasLikeSpecEmitterContext
) extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val namedServersEmitters =
      servers.map(s => new AsyncApiSingleServerEmitter(s, ordering))
    b.entry(
      key,
      _.obj(pb => namedServersEmitters.foreach(e => e.emit(pb)))
    )
  }

  override def position(): Position = servers.headOption.map(b => pos(b.annotations)).getOrElse(ZERO)
}
