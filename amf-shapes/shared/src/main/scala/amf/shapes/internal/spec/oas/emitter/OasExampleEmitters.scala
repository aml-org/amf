package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.Example
import amf.shapes.internal.spec.common.emitter.{NamedMultipleExampleEmitter, ShapeEmitterContext, SingleExampleEmitter}

import scala.collection.mutable.ListBuffer

object OasExampleEmitters {

  def apply(isHeader: Boolean,
            exampleOption: Option[Example],
            ordering: SpecOrdering,
            extensions: Seq[Example],
            references: Seq[BaseUnit])(implicit spec: ShapeEmitterContext): OasExampleEmitters = {
    val label = if (isHeader) "example".asOasExtension else "example"
    new OasExampleEmitters(label, exampleOption, ordering, extensions, references)
  }
}

trait OasLikeExampleEmitters {
  def emitters(): ListBuffer[EntryEmitter]
}

class OasExampleEmitters(label: String,
                         exampleOption: Option[Example],
                         ordering: SpecOrdering,
                         extensions: Seq[Example],
                         references: Seq[BaseUnit])(implicit spec: ShapeEmitterContext)
    extends OasLikeExampleEmitters {

  def emitters(): ListBuffer[EntryEmitter] = {
    val em = ListBuffer[EntryEmitter]()
    exampleOption.foreach(a => em += SingleExampleEmitter(label, a, ordering))
    if (extensions.nonEmpty)
      em += NamedMultipleExampleEmitter("examples".asOasExtension, extensions, ordering, references)
    em
  }
}
