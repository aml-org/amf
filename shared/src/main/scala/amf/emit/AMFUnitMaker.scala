package amf.emit

import amf.client.GenerationOptions
import amf.document.Fragment.{DialectFragment, Fragment}
import amf.document.{BaseUnit, Document, Module}
import amf.domain.dialects.DomainEntity
import amf.graph.GraphEmitter
import amf.remote._
import amf.spec.dialects.DialectEmitter
import amf.spec.oas.{OasDocumentEmitter, OasFragmentEmitter, OasModuleEmitter}
import amf.spec.raml.{RamlDocumentEmitter, RamlFragmentEmitter, RamlModuleEmitter}
import org.yaml.model.YDocument

/**
  * AMF Unit Maker
  */
class AMFUnitMaker {

  def make(unit: BaseUnit, vendor: Vendor, options: GenerationOptions): YDocument = {
    vendor match {
      case Amf | Payload => makeAmfWebApi(unit, options)
      case Raml | Oas    => makeUnitWithSpec(unit, vendor)
      case Unknown       => throw new Exception("Cannot make unit for unknown provider")
    }
  }
  private def isDialect(unit: BaseUnit) = unit match {
    case document: Document => document.encodes.isInstanceOf[DomainEntity]
    case module: Module =>
      module.declares exists {
        case _: DomainEntity => true
        case _               => false
      }
    case _: DialectFragment => true
    case _                  => false
  }

  private def makeUnitWithSpec(unit: BaseUnit, vendor: Vendor): YDocument = {
    vendor match {
      case Raml if isDialect(unit) => makeRamlDialect(unit)
      case Raml                    => makeRamlUnit(unit)
      case Oas                     => makeOasUnit(unit)
      case _                       => throw new IllegalStateException("Invalid vendor " + vendor)
    }
  }

  private def makeRamlDialect(unit: BaseUnit): YDocument = DialectEmitter(unit).emit()

  private def makeRamlUnit(unit: BaseUnit): YDocument = unit match {
    case module: Module     => RamlModuleEmitter(module).emitModule()
    case document: Document => RamlDocumentEmitter(document).emitDocument()
    case fragment: Fragment => new RamlFragmentEmitter(fragment).emitFragment()
    case _                  => throw new IllegalStateException("Invalid base unit form maker")
  }

  private def makeOasUnit(unit: BaseUnit): YDocument = unit match {
    case module: Module     => OasModuleEmitter(module).emitModule()
    case document: Document => OasDocumentEmitter(document).emitDocument()
    case fragment: Fragment => new OasFragmentEmitter(fragment).emitFragment()
    case _                  => throw new IllegalStateException("Invalid base unit form maker")
  }

  private def makeAmfWebApi(unit: BaseUnit, options: GenerationOptions): YDocument = GraphEmitter.emit(unit, options)
}

object AMFUnitMaker {
  def apply(unit: BaseUnit, vendor: Vendor, options: GenerationOptions): YDocument =
    new AMFUnitMaker().make(unit, vendor, options)
}
