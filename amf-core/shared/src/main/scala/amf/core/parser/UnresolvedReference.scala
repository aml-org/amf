package amf.core.parser

import amf.core.model.domain.{DomainElement, Linkable}
import amf.plugins.features.validation.ParserSideValidations.UnresolvedReference

trait UnresolvedReference { this: DomainElement =>
  val reference: String

  // Unresolved references to things that can be linked
  var ctx: Option[ParserContext] = None

  def withContext(c: ParserContext): DomainElement = {
    ctx = Some(c)
    this
  }

  def futureRef(resolve: Linkable => Unit): Unit = ctx match {
    case Some(c) =>
      c.futureDeclarations.futureRef(
        id,
        reference,
        DeclarationPromise(
          resolve,
          () =>
            c.violation(
              UnresolvedReference,
              this,
              None,
              s"Unresolved reference $reference from root context ${c.rootContextDocument}"
          )
        )
      )
    case _ => throw new Exception("Cannot create unresolved reference with missing parsing context")
  }

}
