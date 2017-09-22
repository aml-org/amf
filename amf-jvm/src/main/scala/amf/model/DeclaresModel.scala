package amf.model

import java.util
import scala.collection.JavaConverters._
import scala.language.postfixOps

trait DeclaresModel {

  private[amf] def element: amf.document.DeclaresModel

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  def declares: util.List[DomainElement] =
    element.declares
      .map {
        case node: amf.shape.NodeShape                            => NodeShape(node)
        case scalar: amf.shape.ScalarShape                        => ScalarShape(scalar)
        case tuple: amf.shape.TupleShape                          => TupleShape(tuple)
        case matrix: amf.shape.MatrixShape                        => MatrixShape(matrix)
        case property: amf.domain.extensions.CustomDomainProperty => CustomDomainProperty(property)
        case _                                                    => throw new RuntimeException("unsupported domain element type in module declaration")
        // todo
      } map { de: DomainElement =>
      de
    } asJava
}
