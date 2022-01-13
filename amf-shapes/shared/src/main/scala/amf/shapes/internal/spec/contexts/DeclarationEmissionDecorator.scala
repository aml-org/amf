package amf.shapes.internal.spec.contexts

trait DeclarationEmissionDecorator {
  private var emittingDeclarations: Boolean = false

  def runAsDeclarations(fn: () => Unit): Unit = {
    emittingDeclarations = true
    fn()
    emittingDeclarations = false
  }
}
