package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain.bindings.{
  ChannelBindings,
  MessageBindings,
  OperationBindings,
  ServerBindings
}
import amf.apicontract.client.scala.model.domain._
import amf.apicontract.internal.spec.common.emitter.AgnosticShapeEmitterContextAdapter
import amf.apicontract.internal.spec.oas.emitter.context.OasLikeSpecEmitterContext
import amf.apicontract.internal.spec.spec.OasDefinitions.{
  appendParameterDefinitionsPrefix,
  appendResponsesDefinitionsPrefix
}
import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.render.BaseEmitters.pos
import amf.shapes.internal.spec.common.emitter.{ShapeEmitterContext, ShapeReferenceEmitter}
import amf.shapes.internal.spec.oas.OasShapeDefinitions.appendOas3ComponentsPrefix
import amf.shapes.internal.spec.oas.emitter.OasSpecEmitter

case class OasTagToReferenceEmitter(link: DomainElement)(implicit val specContext: OasLikeSpecEmitterContext)
    extends OasSpecEmitter
    with ShapeReferenceEmitter {

  implicit val shapeSpec = AgnosticShapeEmitterContextAdapter(specContext)

  override protected def getRefUrlFor(element: DomainElement, default: String = referenceLabel)(implicit
      spec: ShapeEmitterContext
  ) = element match {
    case _: Parameter                        => appendParameterDefinitionsPrefix(referenceLabel)
    case _: Payload                          => appendParameterDefinitionsPrefix(referenceLabel)
    case _: Response                         => appendResponsesDefinitionsPrefix(referenceLabel)
    case _: Callback                         => appendOas3ComponentsPrefix(referenceLabel, "callbacks")
    case _: TemplatedLink                    => appendOas3ComponentsPrefix(referenceLabel, "links")
    case _: CorrelationId                    => appendOas3ComponentsPrefix(referenceLabel, "correlationIds")
    case m: Message if m.isAbstract.value()  => appendOas3ComponentsPrefix(referenceLabel, "messageTraits")
    case m: Message if !m.isAbstract.value() => appendOas3ComponentsPrefix(referenceLabel, "messages")
    case _: ServerBindings                   => appendOas3ComponentsPrefix(referenceLabel, "serverBindings")
    case _: OperationBindings                => appendOas3ComponentsPrefix(referenceLabel, "operationBindings")
    case _: ChannelBindings                  => appendOas3ComponentsPrefix(referenceLabel, "channelBindings")
    case _: MessageBindings                  => appendOas3ComponentsPrefix(referenceLabel, "messageBindings")
    case _                                   => super.getRefUrlFor(element, default)
  }

  override def position(): Position = pos(link.annotations)
}
