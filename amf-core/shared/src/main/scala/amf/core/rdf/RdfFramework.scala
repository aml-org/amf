package amf.core.rdf

import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit

trait RdfFramework {

  def emptyRdfModel(): RdfModel

  def unitToRdfModel(unit: BaseUnit, options: RenderOptions): RdfModel = {
    val model = emptyRdfModel()
    new RdfModelEmitter(model).emit(unit, options)
    model
  }

}
