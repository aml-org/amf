package amf.emit

import amf.client.GenerationOptions
import amf.document.{BaseUnit, Document, Module}
import amf.graph.GraphEmitter
import amf.remote.{Amf, Oas, Raml, Vendor}
import amf.spec.oas.{OasDocumentEmitter, OasModuleEmitter}
import amf.spec.raml.{RamlDocumentEmitter, RamlModuleEmitter}
import org.yaml.model.YDocument

/**
  * AMF Unit Maker
  */
class AMFUnitMaker {

  def make(unit: BaseUnit, vendor: Vendor, options: GenerationOptions): YDocument = {
    vendor match {
      case Amf        => makeAmfWebApi(unit, options)
      case Raml | Oas => makeUnitWithSpec(unit, vendor)
    }
  }

  private def makeUnitWithSpec(unit: BaseUnit, vendor: Vendor): YDocument = {
    vendor match {
      case Raml =>
        makeRamlUnit(unit)
      case Oas =>
        makeOasUnit(unit)
      case _ => throw new IllegalStateException("Invalid vendor " + vendor)
    }
  }

  private def makeRamlUnit(unit: BaseUnit): YDocument = unit match {
    case module: Module     => RamlModuleEmitter(module).emitModule()
    case document: Document => RamlDocumentEmitter(document).emitDocument()
    case _                  => throw new IllegalStateException("Invalid base unit form maker")
  }

  private def makeOasUnit(unit: BaseUnit): YDocument = unit match {
    case module: Module     => OasModuleEmitter(module).emitModule()
    case document: Document => OasDocumentEmitter(document).emitDocument()
    case _                  => throw new IllegalStateException("Invalid base unit form maker")
  }

  private def makeAmfWebApi(unit: BaseUnit, options: GenerationOptions): YDocument = GraphEmitter.emit(unit, options)
}

object AMFUnitMaker {
  def apply(unit: BaseUnit, vendor: Vendor, options: GenerationOptions): YDocument =
    new AMFUnitMaker().make(unit, vendor, options)
}
