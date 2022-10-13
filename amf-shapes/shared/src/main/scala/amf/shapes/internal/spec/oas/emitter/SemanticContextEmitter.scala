package amf.shapes.internal.spec.oas.emitter

import org.mulesoft.common.client.lexical.Position
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.render.BaseEmitters.{EntryPartEmitter, ScalarEmitter, pos}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.{BaseIri, ContextMapping, DefaultVocabulary, SemanticContext}
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import org.yaml.model.YDocument.EntryBuilder
import org.yaml.model.{YDocument, YNode, YType}

import scala.collection.immutable
import scala.language.postfixOps

case class SemanticContextEmitter(context: SemanticContext, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext)
    extends EntryEmitter {

  val parts: immutable.Seq[EntryEmitter] = emitBase() ++ emitVocab() ++ emitTypes() ++ emitPrefixes() ++ emitMappings()

  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      "@context",
      f => {
        f.obj { obj =>
          parts.sortBy(p => p.position()).foreach { p =>
            p.emit(obj)
          }
        }
      }
    )
  }

  def emitBase(): List[EntryEmitter] = {
    context.base.map { base =>
      List(SemanticContextBasePartEmitter(base, ordering))
    } getOrElse (Nil)
  }

  def emitVocab(): List[EntryEmitter] = {
    context.vocab.map { vocab =>
      List(SemanticContextVocabPartEmitter(vocab, ordering))
    } getOrElse (Nil)
  }

  def emitTypes(): List[EntryEmitter] = {
    if (context.typeMappings.isEmpty) {
      Nil
    } else {
      List(SemanticContextTypeMappingPartEmitter(context.typeMappings.map(_.value()), ordering))
    }
  }

  def emitPrefixes(): List[EntryEmitter] = {
    context.curies.map { prefix =>
      EntryPartEmitter(
        prefix.alias.value(),
        ScalarEmitter(AmfScalar(prefix.iri.value())),
        YType.Str,
        pos(prefix.annotations)
      )
    } toList
  }

  def emitMappings(): List[EntryEmitter] = {
    context.mapping.map { mapping =>
      SemanticContextMappingPartEmitter(mapping, ordering)
    } toList
  }

  override def position(): Position = pos(context.annotations)

}

private case class SemanticContextBasePartEmitter(base: BaseIri, ordering: SpecOrdering)(implicit
    spec: ShapeEmitterContext
) extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    base.nulled.option().foreach { nulled =>
      if (nulled) {
        b.entry("@base", YNode.Null)
      }
    }
    base.iri.option().foreach { iri =>
      b.entry("@base", YNode(iri))
    }
  }

  override def position(): Position = {
    pos(base.annotations)
  }

}

private case class SemanticContextVocabPartEmitter(vocab: DefaultVocabulary, ordering: SpecOrdering)(implicit
    spec: ShapeEmitterContext
) extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    vocab.iri.option().foreach { iri =>
      b.entry("@vocab", YNode(iri))
    }
  }

  override def position(): Position = {
    pos(vocab.annotations)
  }

}

private case class SemanticContextTypeMappingPartEmitter(types: Seq[String], ordering: SpecOrdering)(implicit
    spec: ShapeEmitterContext
) extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    if (types.size == 1) {
      b.entry("@type", types.head)
    } else {
      b.entry(
        "@type",
        b => {
          b.list { l =>
            types.foreach(l.+=)
          }
        }
      )
    }
  }

  override def position(): Position = {
    Position.ZERO
  }

}

private case class SemanticContextMappingPartEmitter(mapping: ContextMapping, ordering: SpecOrdering)(implicit
    spec: ShapeEmitterContext
) extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit = {
    b.entry(
      mapping.alias.value(),
      { f =>
        if (mapping.iri.nonEmpty && !existsOtherEntry) {
          f.+=(YNode(mapping.iri.value()))
        } else if (existsOtherEntry) {
          f.obj { obj =>
            if (mapping.iri.nonEmpty) { emitIdEntry(obj) }
            if (mapping.coercion.nonEmpty) { emitTypeEntry(obj) }
            if (mapping.container.nonEmpty) { emitContainerEntry(obj) }
          }
        }
      }
    )
  }

  private def existsOtherEntry: Boolean =
    mapping.coercion.nonEmpty || mapping.container.nonEmpty

  private def emitTypeEntry(obj: EntryBuilder): Unit =
    obj.entry("@type", YNode(mapping.coercion.value()))

  private def emitIdEntry(obj: EntryBuilder): Unit =
    obj.entry("@id", YNode(mapping.iri.value()))

  private def emitContainerEntry(obj: EntryBuilder): Unit =
    obj.entry("@container", YNode(mapping.container.value()))

  override def position(): Position = pos(mapping.annotations)
}
