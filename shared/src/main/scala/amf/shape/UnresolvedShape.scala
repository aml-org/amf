package amf.shape

import amf.domain.Annotation.LexicalInformation
import amf.domain.{Annotations, Fields, Linkable}
import amf.spec.{DeclarationPromise, ParserContext}
import amf.validation.model.ParserSideValidations.ParsingErrorSpecification
import org.yaml.model.{YNode, YPart}

/**
  * Unresolved shape: intended to be resolved after parsing (exception is thrown if shape is not resolved).
  */
case class UnresolvedShape(fields: Fields, annotations: Annotations, reference: String) extends Shape {

  override def linkCopy(): Linkable = this

  override def adopted(parent: String): this.type = withId(parent + "/unresolved")

  /** Resolve [[UnresolvedShape]] as link to specified target. */
  def resolve(target: Shape): Shape = target.link(reference, annotations).asInstanceOf[Shape].withName(name)

  // Unresolved references to things that can be linked
  var ctx: Option[ParserContext] = None

  def withContext(c: ParserContext): UnresolvedShape = {
    ctx = Some(c)
    this
  }

  def futureRef(resolve: (Linkable) => Unit): Unit = ctx match {
    case Some(c) =>
      c.declarations.futureRef(
        reference,
        DeclarationPromise(
          resolve,
          () =>
            c.violation(
              ParsingErrorSpecification.id(),
              id,
              None,
              s"Unresolved reference $reference from root context ${c.rootContextDocument}",
              annotations.find(classOf[LexicalInformation])
          )
        )
      )
    case _ => throw new Exception("Cannot create unresolved reference with missing parsing context")
  }
}

object UnresolvedShape {
  def apply(reference: String): UnresolvedShape = apply(reference, Annotations())

  def apply(reference: String, ast: YPart): UnresolvedShape = apply(reference, Annotations(ast))

  def apply(reference: String, ast: Option[YPart]): UnresolvedShape =
    apply(reference, Annotations(ast.getOrElse(YNode.Null)))

  def apply(reference: String, annotations: Annotations): UnresolvedShape =
    UnresolvedShape(Fields(), annotations, reference)
}
