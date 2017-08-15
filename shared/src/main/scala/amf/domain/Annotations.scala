package amf.domain

import amf.common.AMFAST
import amf.domain.Annotation.LexicalInformation

import scala.collection.mutable

/**
  * Element annotations
  */
class Annotations {

  private val annotations: mutable.ListBuffer[Annotation] = mutable.ListBuffer()

  def +(annotation: Annotation): this.type = {
    annotations += annotation
    this
  }
}

object Annotations {

  def apply(): Annotations = new Annotations()

  def apply(ast: AMFAST): Annotations = apply() + LexicalInformation(ast.range)
}
