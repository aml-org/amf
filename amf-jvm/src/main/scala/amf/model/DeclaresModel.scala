package amf.model

import java.util

import amf.domain.extensions
import amf.domain.`abstract`
import amf.shape

import scala.collection.JavaConverters._
import scala.language.postfixOps

trait DeclaresModel {

  private[amf] val element: amf.document.DeclaresModel

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  lazy val declares: util.List[DomainElement] = {
    val declarations: Seq[DomainElement] = element.declares
      .map {
        case node: shape.NodeShape                     => NodeShape(node)
        case scalar: shape.ScalarShape                 => ScalarShape(scalar)
        case tuple: shape.TupleShape                   => TupleShape(tuple)
        case matrix: shape.MatrixShape                 => MatrixShape(matrix)
        case property: extensions.CustomDomainProperty => CustomDomainProperty(property)
        case tr: `abstract`.Trait                      => Trait(tr)
        case resourceType: `abstract`.ResourceType     => ResourceType(resourceType)
        case _                                         => throw new RuntimeException("Unsupported domain element type in module declaration")
      }
    declarations.asJava
  }
}
