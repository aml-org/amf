package amf.plugins.document.apicontract.contexts.emitter.raml

import amf.core.annotations.DomainExtensionAnnotation
import amf.core.emitter.BaseEmitters.{BaseValueEmitter, ValueEmitter, sourceOr}
import amf.core.emitter.EntryEmitter
import amf.core.emitter.SpecOrdering.Default
import amf.core.model.domain.extensions.DomainExtension
import amf.core.parser.FieldEntry
import amf.core.remote.Raml10
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.ShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YNode, YScalar, YType}

object RamlScalarEmitter {
  def apply(key: String, f: FieldEntry, mediaType: Option[YType] = None)(
      implicit spec: ShapeEmitterContext): EntryEmitter = {
    val extensions = f.value.value.annotations.collect({ case e: DomainExtensionAnnotation => e })
    if (extensions.nonEmpty && spec.vendor == Raml10) {
      RamlScalarValueEmitter(key, f, extensions.map(_.extension), mediaType)
    } else {
      ValueEmitter(key, f, mediaType)
    }
  }
}

private case class RamlScalarValueEmitter(key: String,
                                          f: FieldEntry,
                                          extensions: Seq[DomainExtension],
                                          mediaType: Option[YType] = None)(implicit spec: ShapeEmitterContext)
    extends BaseValueEmitter {

  override def emit(b: EntryBuilder): Unit = sourceOr(f.value, annotatedScalar(b))

  private def annotatedScalar(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj { b =>
        b.value = YNode(YScalar(f.scalar.value), mediaType.getOrElse(tag))
        extensions.foreach { e =>
          spec.annotationEmitter(e, Default).emit(b)
        }
      }
    )
  }
}
