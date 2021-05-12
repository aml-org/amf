package amf.plugins.document.webapi.contexts

import amf.core.model.domain.DomainElement

trait DeclarationEmissionDecorator {
  private var emittingDeclarations: Boolean = false

  def runAsDeclarations(fn: () => Unit): Unit = {
    emittingDeclarations = true
    fn()
    emittingDeclarations = false
  }

  def filterLocal[T <: DomainElement](elements: Seq[T]): Seq[T] = {
    if (!emittingDeclarations) elements
    else elements.filter(!_.fromLocal())
  }
}
