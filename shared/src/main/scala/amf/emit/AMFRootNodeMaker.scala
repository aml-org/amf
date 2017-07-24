package amf.emit

import amf.common.AMFToken._
import amf.common.{AMFAST, AMFASTNode}
import amf.domain.WebApi
import amf.parser.Range.NONE
import amf.remote.{Oas, Raml, Vendor}
import amf.spec.Spec

class AMFRootNodeMaker {

  def make(webApi: WebApi, vendor: Vendor): AMFASTNode = {
    val amfast = Spec(vendor).emitter.emit(webApi.fields).build

    vendor match {
      case Raml => new AMFASTNode(Root, "", null, new AMFASTNode(Comment, "%RAML 1.0", null) +: List(amfast))
      case Oas =>
        new AMFASTNode(Root,
                       "",
                       null,
                       List(new AMFASTNode(amfast.`type`, "", null, swaggerEntry() +: amfast.children)))
    }
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

object AMFRootNodeMaker {
  def apply(webApi: WebApi, vendor: Vendor): AMFASTNode = new AMFRootNodeMaker().make(webApi, vendor)

}
