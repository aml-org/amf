package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.annotations.DeclaredElement
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
import amf.plugins.document.webapi.parser.spec.oas.OasSpecEmitter
import amf.plugins.domain.webapi.models.{Callback, Parameter, Payload, Response, TemplatedLink}
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

/**
  *
  */
case class OasTagToReferenceEmitter(
    target: DomainElement,
    label: Option[String],
    reference: Seq[BaseUnit]
)(implicit override val spec: OasLikeSpecEmitterContext)
    extends OasSpecEmitter
    with TagToReferenceEmitter {
  def emit(b: PartBuilder): Unit = {
    follow() match {
      case s: Shape if s.annotations.contains(classOf[DeclaredElement]) =>
        spec.ref(
          b,
          OasDefinitions
            .appendDefinitionsPrefix(referenceLabel, Some(spec.vendor))
        )
      case p: Parameter if p.annotations.contains(classOf[DeclaredElement]) =>
        spec.ref(b, OasDefinitions.appendParameterDefinitionsPrefix(referenceLabel))
      case p: Payload if p.annotations.contains(classOf[DeclaredElement]) =>
        spec.ref(b, OasDefinitions.appendParameterDefinitionsPrefix(referenceLabel))
      case r: Response if r.annotations.contains(classOf[DeclaredElement]) =>
        spec.ref(b, OasDefinitions.appendResponsesDefinitionsPrefix(referenceLabel))
      case c: Callback if c.annotations.contains(classOf[DeclaredElement]) =>
        spec.ref(b, OasDefinitions.appendOas3ComponentsPrefix(referenceLabel, "callbacks"))
      case c: TemplatedLink if c.annotations.contains(classOf[DeclaredElement]) =>
        spec.ref(b, OasDefinitions.appendOas3ComponentsPrefix(referenceLabel, "links"))
      case _ => spec.ref(b, referenceLabel)
    }
  }

  /** Follow links until first declaration or last element in chain */
  private def follow(): DomainElement = follow(target)

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

  override def position(): Position = pos(target.annotations)
}

case class RamlTagToReferenceEmitter(target: DomainElement, label: Option[String], references: Seq[BaseUnit])(
    implicit val spec: RamlSpecEmitterContext)
    extends PartEmitter
    with TagToReferenceEmitter {
  override def emit(b: PartBuilder): Unit = {
    references.find {
      case m: Module   => m.declares.contains(target)
      case f: Fragment => f.encodes == target
    } match {
      case Some(_: Fragment) => spec.ref(b, referenceLabel)
      case _                 => raw(b, referenceLabel)
    }
  }

  override def position(): Position = pos(target.annotations)
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
