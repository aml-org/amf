package amf.emit

import amf.common.{AMFAST, AMFASTNode}
import amf.common.AMFToken.{Comment, Entry, Root, StringToken}
import amf.document.{BaseUnit, Document}
import amf.domain.WebApi
import amf.graph.GraphEmitter
import amf.parser.Range.NONE
import amf.remote.{Amf, Oas, Raml, Vendor}
import amf.spec.Spec

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
    unit match {
      case document: Document =>
        document.encodes match {
          case api: WebApi => makeWebApiWithSpec(api, vendor)
        }
    }
  }

  private def makeWebApiWithSpec(api: WebApi, vendor: Vendor): AMFAST = {
    vendor match {
      case Raml => makeRamlWebApi(api)
      case Oas  => makeOasWebApi(api)
    }
  }

  private def makeAmfWebApi(unit: BaseUnit): AMFAST = GraphEmitter.emit(unit)

  private def makeOasWebApi(api: WebApi) = {
    val ast = Spec(Oas).emitter.emit(api.fields).build
    new AMFASTNode(Root, "", null, List(new AMFASTNode(ast.`type`, "", null, swaggerEntry() +: ast.children)))
  }

  private def makeRamlWebApi(api: WebApi) = {
    val ast = Spec(Raml).emitter.emit(api.fields).build
    new AMFASTNode(Root, "", null, new AMFASTNode(Comment, "%RAML 1.0", null) +: List(ast))
  }

  private def swaggerEntry(): AMFAST = {
    new AMFASTNode(Entry,
                   "",
                   NONE,
                   List(
                     new AMFASTNode(StringToken, "swagger", NONE),
                     new AMFASTNode(StringToken, "2.0", NONE)
                   ))
  }
}

object AMFUnitMaker {
  def apply(unit: BaseUnit, vendor: Vendor): AMFAST = new AMFUnitMaker().make(unit, vendor)
}
