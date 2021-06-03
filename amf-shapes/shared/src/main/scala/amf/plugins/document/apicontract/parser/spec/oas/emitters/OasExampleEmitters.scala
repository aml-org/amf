package amf.plugins.document.apicontract.parser.spec.oas.emitters

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.ShapeEmitterContext
import amf.plugins.document.apicontract.parser.spec.domain.{NamedMultipleExampleEmitter, SingleExampleEmitter}
import amf.plugins.domain.shapes.models.Example

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
