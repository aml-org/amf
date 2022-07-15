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

/** Produces a link from ${source} and executes a ${callback} with the resulting link
  * @param source
  *   source
  * @param callback
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
case class LinkAction[F[_], S, T <: DomainElement, C <: WebApiContext](source: F[S])(callback: F[T] => Unit) {
  def execute()(implicit linker: Linker[F, S, T, C], ctx: C): Unit = callback(linker.link(source))
}
