package amf.apicontract.internal.spec.async.emitters.domain
import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.internal.spec.common.emitter.{OasServerVariableEmitter, OasServerVariablesEmitter}
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeSpecEmitterContext
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import org.mulesoft.common.client.lexical.Position
import org.mulesoft.common.client.lexical.Position.ZERO
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

case class AsyncApiServerVariablesDeclarationEmitter(
    key: String,
    serverVariable: Seq[Parameter],
    ordering: SpecOrdering
)(implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val namedServersVariableEmitters = serverVariable.map(s => OasServerVariableEmitter(s, ordering))
    b.entry(
      key,
      _.obj(pb => namedServersVariableEmitters.foreach(e => e.emit(pb)))
    )
  }
  override def position(): Position = serverVariable.headOption.map(b => pos(b.annotations)).getOrElse(ZERO)
}
