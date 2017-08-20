package amf.emit

import amf.common.AMFAST
import amf.document.BaseUnit
import amf.graph.GraphEmitter
import amf.remote.{Amf, Oas, Raml, Vendor}
import amf.spec.oas.OasSpecEmitter
import amf.spec.raml.RamlSpecEmitter

/**
  * AMF Unit Maker
  */
class AMFUnitMaker {

  def make(unit: BaseUnit, vendor: Vendor): AMFAST = {
    vendor match {
      case Amf        => makeAmfWebApi(unit)
      case Raml | Oas => makeUnitWithSpec(unit, vendor)
    }
  }

  private def makeUnitWithSpec(unit: BaseUnit, vendor: Vendor): AMFAST = {
    vendor match {
      case Raml =>
        RamlSpecEmitter(unit).emitWebApi()
      case Oas =>
        OasSpecEmitter(unit).emitWebApi()
      case _ => throw new IllegalStateException("Invalid vendor " + vendor)
    }
  }

  private def makeAmfWebApi(unit: BaseUnit): AMFAST = GraphEmitter.emit(unit)
}

object AMFUnitMaker {
  def apply(unit: BaseUnit, vendor: Vendor): AMFAST = new AMFUnitMaker().make(unit, vendor)
}
