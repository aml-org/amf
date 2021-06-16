package amf.apicontract.internal.spec.async.emitters.bindings

import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.parser.domain.Fields
import amf.core.internal.render.BaseEmitters.ValueEmitter
import amf.core.internal.render.emitters.EntryEmitter
import amf.apicontract.internal.metamodel.domain.bindings.BindingVersion

import scala.collection.mutable.ListBuffer

abstract class AsyncApiCommonBindingEmitter() extends EntryEmitter {

  def emitBindingVersion(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
    fs.entry(BindingVersion.BindingVersion).foreach { f =>
      if (!f.value.annotations.contains(classOf[SynthesizedField])) result += ValueEmitter("bindingVersion", f)
    }
  }
}
