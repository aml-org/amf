package amf.plugins.document.webapi

import amf.plugins.document.webapi.metamodel.{ExtensionModel, OverlayModel}

trait WebApiDocuments {

  def webApiDocuments = Seq(
    ExtensionModel,
    OverlayModel
  )
}
