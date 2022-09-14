package amf.apicontract.internal.plugins

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document.{ASTRefContainer, ParserContext, Reference}
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec
import amf.core.internal.validation.CoreValidations.InvalidCrossSpec
import org.mulesoft.common.client.lexical.SourceLocation

trait CrossSpecRestriction { this: SpecAwareParsePlugin =>

  protected def restrictCrossSpecReferences(referenceSpec: Option[Spec], reference: Reference)(implicit
      errorHandler: AMFErrorHandler
  ): Unit = {
    referenceSpec.foreach { referencedSpec =>
      if (!validSpecsToReference.contains(referencedSpec)) {
        referenceNodes(reference).foreach(node =>
          errorHandler.violation(
            InvalidCrossSpec,
            "",
            s"Cannot reference a ${referencedSpec.id} spec from a different spec",
            node
          )
        )
      }
    }
  }

  protected def restrictCrossSpecReferences(document: Root, ctx: ParserContext): Unit = {
    restrictCrossSpecReferences(document, ctx.eh)
  }

  protected def restrictCrossSpecReferences(document: Root, eh: AMFErrorHandler): Unit = {
    document.references.foreach { r =>
      restrictCrossSpecReferences(r.unit.sourceSpec, r.origin)(eh)
    }
  }

  private def referenceNodes(reference: Reference): Seq[SourceLocation] = reference.refs.collect {
    case ref: ASTRefContainer => ref.pos
  }
}
