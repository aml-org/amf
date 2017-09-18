package amf.emit

import amf.client.GenerationOptions
import amf.common.AMFAST
import amf.dialects.{DialectEmitter, DomainEntity}
import amf.document.{BaseUnit, Document}
import amf.domain.WebApi
import amf.graph.GraphEmitter
import amf.remote.{Amf, Oas, Raml, Vendor}
import amf.spec.oas.OasSpecEmitter
import amf.spec.raml.RamlSpecEmitter

/**
  * AMF Unit Maker
  */
class AMFUnitMaker {

  def make(unit: BaseUnit, vendor: Vendor, options: GenerationOptions): AMFAST = {
    vendor match {
      case Amf        => makeAmfWebApi(unit, options)
      case Raml | Oas => makeUnitWithSpec(unit, vendor)
    }
  }
  private def isDialect(unit:BaseUnit) = unit match {
    case document: Document => document.encodes.isInstanceOf[DomainEntity]
  }

  private def makeUnitWithSpec(unit: BaseUnit, vendor: Vendor): AMFAST = {
    vendor match {
      case Raml =>
        if (isDialect(unit)) DialectEmitter(unit).emit()
        else RamlSpecEmitter(unit).emitDocument()
      case Oas =>
        OasSpecEmitter(unit).emitDocument()
      case _ => throw new IllegalStateException("Invalid vendor " + vendor)
    }
  }

  private def makeAmfWebApi(unit: BaseUnit, options: GenerationOptions): AMFAST = GraphEmitter.emit(unit, options)
}

object AMFUnitMaker {
  def apply(unit: BaseUnit, vendor: Vendor, options: GenerationOptions): AMFAST =
    new AMFUnitMaker().make(unit, vendor, options)
}
