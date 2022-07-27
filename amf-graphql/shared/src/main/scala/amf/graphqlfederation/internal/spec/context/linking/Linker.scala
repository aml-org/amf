package amf.graphqlfederation.internal.spec.context.linking

import amf.apicontract.internal.spec.common.parser.WebApiContext
import amf.core.client.scala.model.domain.DomainElement

import scala.language.higherKinds

/** Class that is able to produce links to any domain element
  * @tparam F
  *   is an optional wrapper of arity 1 e.g. Seq, List, Set
  * @tparam S
  *   link source type
  * @tparam T
  *   link target type < DomainElement
  * @tparam C
  *   context
  */
trait Linker[F[_], S, T <: DomainElement, C <: WebApiContext] {
  def link(source: F[S])(implicit ctx: C): F[T]
}

/** Produces a link from source and executes a afterLinking with the resulting link
  * @param source
  *   source
  * @param afterLinking
  *   callback
  * @tparam F
  *   is an optional wrapper of arity 1 e.g. Seq, List, Set
  * @tparam S
  *   link source type
  * @tparam T
  *   link target type < DomainElement
  * @tparam C
  *   context
  */
case class LinkEvaluation[F[_], S, T <: DomainElement, C <: WebApiContext](source: F[S])(afterLinking: F[T] => Unit) {
  def eval()(implicit linker: Linker[F, S, T, C], ctx: C): Unit = {
    val result = linker.link(source)
    afterLinking(result)
  }
}
