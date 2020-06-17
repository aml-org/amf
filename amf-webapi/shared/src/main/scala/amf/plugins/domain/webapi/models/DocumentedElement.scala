package amf.plugins.domain.webapi.models

import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.metamodel.WebApiModel.Documentations

trait DocumentedElement {
  def documentations: Seq[CreativeWork]
}
