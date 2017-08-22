package amf.spec

import amf.domain.Annotation.SourceVendor
import amf.domain.Annotations
import amf.remote.Vendor

/**
  * Created by pedro.colunga on 8/22/17.
  */
object SpecOrdering {

  object Default extends Ordering[Emitter] {
    override def compare(x: Emitter, y: Emitter): Int = 1
  }

  object Lexical extends Ordering[Emitter] {
    override def compare(x: Emitter, y: Emitter): Int = x.position().compareTo(y.position())
  }

  def ordering(target: Vendor, annotations: Annotations): Ordering[Emitter] = {
    annotations.find(classOf[SourceVendor]) match {
      case Some(SourceVendor(source)) if source == target => Lexical
      case _                                              => Default
    }
  }
}
