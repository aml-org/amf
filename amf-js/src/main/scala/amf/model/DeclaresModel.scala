package amf.model

import amf.plugins.domain.shapes.models
import amf.plugins.domain.webapi
import amf.plugins.domain.webapi.models.templates

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

trait DeclaresModel {

  private[amf] def element: amf.framework.model.document.DeclaresModel

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  lazy val declares: js.Iterable[DomainElement] = {
    val declarations = element.declares
      .map {
        case node: models.NodeShape                     => NodeShape(node)
        case scalar: models.ScalarShape                 => ScalarShape(scalar)
        case tuple: models.TupleShape                   => TupleShape(tuple)
        case matrix: models.MatrixShape                 => MatrixShape(matrix)
        case property: webapi.models.CustomDomainProperty => CustomDomainProperty(property)
        case tr: templates.Trait                      => Trait(tr)
        case resourceType: templates.ResourceType     => ResourceType(resourceType)
        case security: amf.plugins.domain.webapi.models.security.SecurityScheme         => SecurityScheme(security)
        case _                                         => throw new RuntimeException("unsupported domain element type in module declaration")
      }
    declarations.toJSArray
  }

}
