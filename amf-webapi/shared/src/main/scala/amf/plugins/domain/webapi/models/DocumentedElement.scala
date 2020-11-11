package amf.plugins.domain.webapi.models

import amf.plugins.domain.shapes.models.CreativeWork

trait DocumentedElement {
  def documentations: Seq[CreativeWork]
}
