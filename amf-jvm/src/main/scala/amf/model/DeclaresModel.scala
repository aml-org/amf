package amf.model

import java.util

import amf.domain.{`abstract`, extensions, security}
import amf.plugins.domain.shapes.models

import scala.collection.JavaConverters._
import scala.language.postfixOps

trait DeclaresModel {

  private[amf] val element: amf.framework.model.document.DeclaresModel

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  lazy val declares: util.List[DomainElement] = {
    val declarations: Seq[DomainElement] = element.declares
      .map {
        case node: models.NodeShape                     => NodeShape(node)
        case scalar: models.ScalarShape                 => ScalarShape(scalar)
        case tuple: models.TupleShape                   => TupleShape(tuple)
        case matrix: models.MatrixShape                 => MatrixShape(matrix)
        case property: extensions.CustomDomainProperty => CustomDomainProperty(property)
        case tr: `abstract`.Trait                      => Trait(tr)
        case resourceType: `abstract`.ResourceType     => ResourceType(resourceType)
        case security: security.SecurityScheme         => SecurityScheme(security)
        case _                                         => throw new RuntimeException("Unsupported domain element type in module declaration")
      }
    declarations.asJava
  }
}
