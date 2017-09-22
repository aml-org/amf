package amf.model

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

trait DeclaresModel {

  private[amf] def element: amf.document.DeclaresModel

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  val declares: js.Iterable[DomainElement] = element.declares
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
  } toJSArray
}
