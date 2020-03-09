package amf.plugins.document.webapi.parser.spec.oas.emitters

import amf.core.utils.AmfStrings
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.domain.{
  ExampleArrayEmitter,
  ExampleValuesEmitter,
  MultipleExampleEmitter,
  NamedMultipleExampleEmitter,
  SingleExampleEmitter
}
import amf.plugins.domain.shapes.models.Example

import scala.collection.mutable.ListBuffer

object OasExampleEmitters {

  def apply(isHeader: Boolean,
            exampleOption: Option[Example],
            ordering: SpecOrdering,
            extensions: Seq[Example],
            references: Seq[BaseUnit])(implicit spec: OasLikeSpecEmitterContext): OasExampleEmitters = {
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
                         references: Seq[BaseUnit])(implicit spec: OasLikeSpecEmitterContext)
    extends OasLikeExampleEmitters {

  def emitters(): ListBuffer[EntryEmitter] = {
    val em = ListBuffer[EntryEmitter]()
    exampleOption.foreach(a => em += SingleExampleEmitter(label, a, ordering))
    if (extensions.nonEmpty)
      em += NamedMultipleExampleEmitter("examples".asOasExtension, extensions, ordering, references)
    em
  }
}
