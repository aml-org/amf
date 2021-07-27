package amf.apicontract.internal.plugins

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document.{ASTRefContainer, ParserContext, Reference}
import amf.core.internal.parser.Root
import amf.core.internal.remote.Vendor
import amf.core.internal.validation.CoreValidations.InvalidCrossSpec
import org.mulesoft.lexer.SourceLocation

trait CrossSpecRestriction { this: ApiParsePlugin =>

  // TODO: all documents should have a Vendor
  protected def restrictCrossSpecReferences(optionalReferencedVendor: Option[Vendor], reference: Reference)(
      implicit errorHandler: AMFErrorHandler): Unit = {
    val possibleReferencedVendors = mediaTypes ++ validMediaTypesToReference
    optionalReferencedVendor.foreach { referencedVendor =>
      if (!possibleReferencedVendors.contains(referencedVendor.mediaType)) {
        referenceNodes(reference).foreach(position =>
          errorHandler.violation(InvalidCrossSpec, "", "Cannot reference fragments of another spec", position))
      }
    }
  }

  protected def restrictCrossSpecReferences(document: Root, ctx: ParserContext): Unit = {
    document.references.foreach { r =>
      restrictCrossSpecReferences(r.unit.sourceVendor, r.origin)(ctx.eh)
    }
  }

  private def referenceNodes(reference: Reference): Seq[SourceLocation] = reference.refs.collect { case ref: ASTRefContainer => ref.pos }
}
