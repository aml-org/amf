package amf.core.rdf

import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.parser.ParsedDocument

case class RdfModelDocument(model: RdfModel) extends ParsedDocument

trait RdfFramework {

  def emptyRdfModel(): RdfModel

  def unitToRdfModel(unit: BaseUnit, options: RenderOptions): RdfModel = {
    val model = emptyRdfModel()
    new RdfModelEmitter(model).emit(unit, options)
    model
  }

  def syntaxToRdfModel(mediaType: String, text: CharSequence): Option[RdfModelDocument] = {
    val model = emptyRdfModel()
    model.load(mediaType, text.toString)
    Some(RdfModelDocument(model))
  }

}
