package amf.core.emitter

import amf.core.annotations.SourceVendor
import amf.core.parser.Annotations
import amf.core.remote.{Amf, Vendor}

/**
  * Created by pedro.colunga on 8/22/17.
  */
object SpecOrdering {

  object Default extends SpecOrdering {
    override def sorted[T <: Emitter](values: Seq[T]): Seq[T] = values

    override def compare(x: Emitter, y: Emitter): Int = 1
  }

  object Lexical extends SpecOrdering {
    def sorted[T <: Emitter](values: Seq[T]): Seq[T] = values.partition(_.position().isZero) match {
      case (without, lexical) => lexical.sorted(this) ++ without
    }

    override def compare(x: Emitter, y: Emitter): Int = x.position().compareTo(y.position())
  }

  def ordering(target: Vendor, annotations: Annotations): SpecOrdering = {
    annotations.find(classOf[SourceVendor]) match {
      case Some(SourceVendor(source)) if source == Amf || source == target => Lexical
      case _                                                               => Default
    }
  }
}

trait SpecOrdering extends Ordering[Emitter] {

  /** Return sorted values. */
  def sorted[T <: Emitter](values: Seq[T]): Seq[T]
}
