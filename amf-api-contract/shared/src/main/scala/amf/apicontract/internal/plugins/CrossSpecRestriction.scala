package amf.apicontract.internal.plugins

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document.{ParserContext, Reference, SYamlRefContainer}
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec
import amf.core.internal.validation.CoreValidations.InvalidCrossSpec
import org.yaml.model.YNode

trait CrossSpecRestriction { this: ApiParsePlugin =>

  // TODO: all documents should have a Vendor
  protected def restrictCrossSpecReferences(optionalReferencedSpec: Option[Spec], reference: Reference)(
      implicit errorHandler: AMFErrorHandler): Unit = {
    val possibleReferencedSpec: List[Spec] = (optionalReferencedSpec ++ validSpecsToReference).toList
    optionalReferencedSpec.foreach { referencedSpec =>
      if (!possibleReferencedSpec.contains(referencedSpec)) {
        referenceNodes(reference).foreach(node =>
          errorHandler.violation(InvalidCrossSpec, "", "Cannot reference fragments of another spec", node))
      }
    }
  }

  protected def restrictCrossSpecReferences(document: Root, ctx: ParserContext): Unit = {
    document.references.foreach { r =>
      restrictCrossSpecReferences(r.unit.sourceSpec, r.origin)(ctx.eh)
    }
  }

  private def referenceNodes(reference: Reference): Seq[YNode] = reference.refs.collect { case ref: SYamlRefContainer => ref.node }
}
