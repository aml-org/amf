package amf.plugins.document.apicontract.parser.spec.declaration

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.{BaseUnit, Fragment}
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.annotations.ExternalFragmentRef
import amf.core.internal.render.BaseEmitters.{pos, raw}
import amf.core.internal.render.emitters.PartEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.ShapeEmitterContext
import org.yaml.model.YDocument.PartBuilder

case class RamlTagToReferenceEmitter(link: DomainElement, references: Seq[BaseUnit])(
    implicit val spec: ShapeEmitterContext)
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
