package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.{DeclaredElement, ExternalFragmentRef}
import amf.core.emitter.BaseEmitters._
import amf.core.emitter.PartEmitter
import amf.core.model.document.{BaseUnit, Fragment, Module}
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.contexts.TagToReferenceEmitter
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.OasDefinitions._
import amf.plugins.document.webapi.parser.spec.oas.OasSpecEmitter
import amf.plugins.domain.webapi.models
import amf.plugins.domain.webapi.models.bindings.{ChannelBindings, MessageBindings, OperationBindings, ServerBindings}
import amf.plugins.domain.webapi.models.{Callback, CorrelationId, Message, Parameter, Payload, Response, TemplatedLink}
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

/**
  *
  */
case class OasTagToReferenceEmitter(link: DomainElement)(implicit override val spec: OasLikeSpecEmitterContext)
    extends OasSpecEmitter
    with TagToReferenceEmitter {

  def emit(b: PartBuilder): Unit = {
    val lastElementInLinkChain = follow()
    val urlToEmit =
      if (isDeclaredElement(lastElementInLinkChain)) getRefUrlFor(lastElementInLinkChain) else referenceLabel
    spec.ref(b, urlToEmit)
  }

  private def getRefUrlFor(element: DomainElement, default: String = referenceLabel) = element match {
    case _: Shape                            => appendSchemasPrefix(referenceLabel, Some(spec.vendor))
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
    case _                                   => default
  }

  private def isDeclaredElement(element: DomainElement) = element.annotations.contains(classOf[DeclaredElement])

  /** Follow links until first declaration or last element in chain */
  private def follow(): DomainElement = follow(link)

  @scala.annotation.tailrec
  private def follow(element: DomainElement, seenLinks: Seq[String] = Seq()): DomainElement = {
    element match {
      case s: Linkable if s.isLink =>
        s.linkTarget match {
          case Some(t: Linkable) if t.isLink & !t.annotations.contains(classOf[DeclaredElement]) =>
            // If find link which is not a declaration (declarations can be links as well) follow link
            follow(t.linkTarget.get, seenLinks :+ element.id)
          case Some(t) => t
          case None    => s // This is unreachable
        }
      case other => other
    }
  }

  override def position(): Position = pos(link.annotations)
}

case class RamlTagToReferenceEmitter(link: DomainElement, references: Seq[BaseUnit])(
    implicit val spec: RamlSpecEmitterContext)
    extends PartEmitter
    with TagToReferenceEmitter {

  override def emit(b: PartBuilder): Unit = {
    if (containsRefAnnotation)
      link.annotations.find(classOf[ExternalFragmentRef]).foreach { a =>
        spec.ref(b, a.fragment) // emits with !include
      } else if (linkReferencesFragment)
      spec.ref(b, referenceLabel) // emits with !include
    else
      raw(b, referenceLabel)
  }

  private def containsRefAnnotation = link.annotations.contains(classOf[ExternalFragmentRef])

  private def linkReferencesFragment: Boolean = {
    link match {
      case l: Linkable =>
        l.linkTarget.exists { target =>
          references.exists {
            case f: Fragment => f.encodes == target
            case _           => false
          }
        }
      case _ => false
    }
  }

  override def position(): Position = pos(link.annotations)
}

class RamlLocalReferenceEntryEmitter(override val key: String, reference: Linkable)
    extends EntryPartEmitter(key, RamlLocalReferenceEmitter(reference))

case class RamlLocalReferenceEmitter(reference: Linkable) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = reference.linkLabel.option() match {
    case Some(label) => raw(b, label)
    case None        => throw new Exception("Missing link label")
  }

  override def position(): Position = pos(reference.annotations)
}

case class RamlIncludeReferenceEmitter(reference: Linkable, location: String) extends PartEmitter {

  override def emit(b: PartBuilder): Unit =
    raw(b, s"!include ${location}", YType.Include)

  override def position(): Position = pos(reference.annotations)
}
