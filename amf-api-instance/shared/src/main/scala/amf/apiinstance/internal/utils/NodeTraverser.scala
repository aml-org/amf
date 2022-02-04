package amf.apiinstance.internal.utils

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.domain.AmfObject
import org.yaml.model.{YMap, YNode, YScalar, YSequence}
import amf.core.internal.parser.{Root, YMapOps, YNodeLikeOps}
import amf.core.internal.validation.CoreValidations.SyamlError

case class TraverseWrapper(node: Option[YNode], eh: AMFErrorHandler, errorInfo: Option[String]) {

  var errorId: Option[String] = errorInfo

  def NullResponse: TraverseWrapper = TraverseWrapper(None, eh, errorId)

  def errorFor(model: AmfObject): TraverseWrapper = {
    this.errorId = Some(model.id)
    this
  }

  private def wrap(n: YNode) = TraverseWrapper(Some(n), eh, errorId)

  def fetch(key: String): TraverseWrapper = {
    node match {
      case Some(n: YNode) => {
        n.to[YMap] match {
          case Right(m) =>
            m.key(key) match {
              case Some(v) => wrap(v.value)
              case _       =>
                errorId.foreach { id =>
                  eh.violation(SyamlError, id, s"Expected `${key}` property in map", m.location)
                }
                NullResponse
            }
          case _        =>
            errorId.foreach { id =>
              eh.violation(SyamlError, id, s"Expected `${key}` property in map", n.location)
            }
            NullResponse

        }
      }
      case _              =>
        errorId.foreach { id =>
          eh.violation(SyamlError, id, s"Expected `${key}` property in map")
        }
        NullResponse
    }
  }

  def string(): Option[String] = {
    node match {
      case Some(v) => v.to[YScalar] match {
        case Right(s) => Some(s.text)
        case _        =>
          errorId.foreach { id =>
            eh.violation(SyamlError, id, s"expected scalar value", v.location)
          }
          None
      }
      case _       =>
        errorId.foreach { id =>
          eh.violation(SyamlError, id, s"expected scalar value found null")
        }
        None
    }
  }

  def stringOr(default: String): String = {
    string match {
      case Some(s) => s
      case _       => default
    }
  }

  def array[T](f:Option[YSequence] => T): T = {
    node match {
      case Some(v) => v.to[YSequence] match {
        case Right(s) => f(Some(s))
        case _        =>
          errorId.foreach { id =>
            eh.violation(SyamlError, id, s"expected collection not found", v.location)
          }
          f(None)
      }
      case _       =>
        errorId.foreach { id =>
          eh.violation(SyamlError, id, s"expected scalar value found null")
        }
        f(None)
    }
  }

  def arrayOr[T](default: T)(f: YSequence => T): T = {
    array {
      case Some(s) => f(s)
      case _       => default
    }
  }

  def map[T](f:Option[YMap] => T) : T = {
    node match {
      case Some(v) => v.to[YMap] match {
        case Right(s) => f(Some(s))
        case _        =>
          errorId.foreach { id =>
            eh.violation(SyamlError, id, s"expected map not found")
          }
          f(None)
      }
      case _       =>
        errorId.foreach { id =>
          eh.violation(SyamlError, id, s"expected map value found null")
        }
        f(None)
    }
  }

  def mapOr[T](default: T)(f:YMap => T): T = {
    map {
      case Some(m) => f(m)
      case _       => default
    }
  }

}

trait NodeTraverser {

  def traverse(node: Any): TraverseWrapper = {
    node match {
      case n:YNode   => TraverseWrapper(Some(n), error_handler, None)
      case n:YMap    => TraverseWrapper(Some(n), error_handler, None)
      case _         => TraverseWrapper(None, error_handler, None)
    }
  }

  def error_handler: AMFErrorHandler

  def collect[T](x:T): T = x
}
