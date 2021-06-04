package amf.plugins.document.apicontract.parser.spec.async.emitters.bindings

import amf.core.annotations.SynthesizedField
import amf.core.emitter.BaseEmitters.ValueEmitter
import amf.core.emitter.EntryEmitter
import amf.core.parser.Fields
import amf.plugins.domain.apicontract.metamodel.bindings.BindingVersion

import scala.collection.mutable.ListBuffer

abstract class AsyncApiCommonBindingEmitter() extends EntryEmitter {

  def emitBindingVersion(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
    fs.entry(BindingVersion.BindingVersion).foreach { f =>
      if (!f.value.annotations.contains(classOf[SynthesizedField])) result += ValueEmitter("bindingVersion", f)
    }
  }
}
