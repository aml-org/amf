package amf.plugins.document.webapi.contexts.emitter

import amf.core.emitter.{PartEmitter, ShapeRenderOptions}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.{DomainElement, Linkable}
import amf.plugins.document.webapi.contexts.emitter.oas.OasRefEmitter
import amf.plugins.document.webapi.contexts.{RefEmitter, SpecEmitterContext, SpecEmitterFactory}

import scala.collection.mutable

abstract class OasLikeSpecEmitterFactory(implicit val spec: OasLikeSpecEmitterContext) extends SpecEmitterFactory {}

abstract class OasLikeSpecEmitterContext(eh: ErrorHandler,
                                         refEmitter: RefEmitter = OasRefEmitter,
                                         options: ShapeRenderOptions = ShapeRenderOptions())
    extends SpecEmitterContext(eh, refEmitter, options) {

  def schemasDeclarationsPath: String

  override def localReference(reference: Linkable): PartEmitter =
    factory.tagToReferenceEmitter(reference.asInstanceOf[DomainElement], reference.linkLabel.option(), Nil)

  val factory: OasLikeSpecEmitterFactory

  val jsonPointersMap: mutable.Map[String, String] = mutable.Map() // id -> pointer

}
