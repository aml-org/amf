package amf.plugins.document.webapi.parser.spec.declaration

import amf.core.model.document.{BaseUnit, Fragment, Module}
import amf.core.model.domain.{DomainElement, Linkable, Shape}
import amf.core.parser.Position
import amf.core.remote.{Oas, Raml, Vendor}
import amf.plugins.document.webapi.annotations.DeclaredElement
import amf.plugins.document.webapi.parser.spec.common.BaseEmitters.{MapEntryEmitter, _}
import amf.plugins.document.webapi.parser.spec.oas.OasSpecEmitter
import amf.plugins.document.webapi.parser.spec.raml.RamlSpecEmitter
import amf.plugins.document.webapi.parser.spec.{OasDefinitions, PartEmitter}
import amf.plugins.domain.webapi.models.Parameter
import org.yaml.model.YDocument.PartBuilder

/**
  *
  */
object TagToReferenceEmitter {
  def apply(target: DomainElement,
            label: Option[String],
            spec: Vendor,
            references: Seq[BaseUnit] = Nil): TagToReferenceEmitter = spec match {
    case Oas   => OasTagToReferenceEmitter(target, label)
    case Raml  => RamlTagToReferenceEmitter(target, label.getOrElse(target.id), references)
    case other => throw new IllegalArgumentException(s"Unsupported vendor $other for tag generation")
  }
}

trait TagToReferenceEmitter extends PartEmitter

case class OasTagToReferenceEmitter(target: DomainElement, label: Option[String])
    extends OasSpecEmitter
    with TagToReferenceEmitter {
  override def emit(b: PartBuilder): Unit = {
    val reference = label.getOrElse(target.id)
    follow() match {
      case s: Shape if s.annotations.contains(classOf[DeclaredElement]) =>
        spec.ref(b, OasDefinitions.appendDefinitionsPrefix(reference))
      case p: Parameter if p.annotations.contains(classOf[DeclaredElement]) =>
        spec.ref(b, OasDefinitions.appendParameterDefinitionsPrefix(reference))
      case _ => spec.ref(b, reference)
    }
  }

  /** Follow links. */
  private def follow(): DomainElement = {
    target match {
      case s: Linkable if s.isLink =>
        s.linkTarget match {
          case Some(t) => t
          case _       => throw new Exception(s"Expected shape link target on $target")
        }
      case other => other
    }
  }

  override def position(): Position = pos(target.annotations)
}

case class OasRefEmitter(url: String, position: Position = Position.ZERO) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = b.obj(MapEntryEmitter("$ref", url).emit(_))

}

case class RamlTagToReferenceEmitter(reference: DomainElement, text: String, references: Seq[BaseUnit])
    extends RamlSpecEmitter
    with TagToReferenceEmitter
    with PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    references.find {
      case m: Module   => m.declares.contains(reference)
      case f: Fragment => f.encodes == reference
    } match {
      case Some(_: Fragment) => spec.ref(b, text)
      case _                 => raw(b, text)
    }
  }

  override def position(): Position = pos(reference.annotations)
}

case class RamlLocalReferenceEmitter(reference: Linkable) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = reference.linkLabel match {
    case Some(label) => raw(b, label)
    case None        => throw new Exception("Missing link label")
  }

  override def position(): Position = pos(reference.annotations)
}
