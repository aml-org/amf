package amf.apicontract.internal.spec.raml

import amf.apicontract.internal.plugins.ApiParsePlugin
import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.spec.raml.parser.document
import amf.apicontract.internal.spec.raml.reference.RamlReferenceHandler
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.exception.InvalidDocumentHeaderException
import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment, Module}
import amf.core.client.scala.model.domain.ExternalDomainElement
import amf.core.client.scala.parse.document._
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec
import amf.core.internal.validation.CoreValidations.{ExpectedModule, InvalidFragmentRef, InvalidInclude}
import org.yaml.model.YNode
import org.yaml.model.YNode.MutRef

trait RamlParsePlugin extends ApiParsePlugin {

  override def allowRecursiveReferences: Boolean = false

  override def referenceHandler(eh: AMFErrorHandler): ReferenceHandler = new RamlReferenceHandler(this)

  override def parse(root: Root, ctx: ParserContext): BaseUnit = {
    val updated = context(ctx, root, ctx.parsingOptions)
    restrictCrossSpecReferences(root, updated)
    inlineExternalReferences(root, updated)

    validateReferences(root.references, ctx)
    RamlHeader(root) match { // todo review this, should we use the raml web api context for get the version parser?
      case Some(f: RamlFragment) => document.RamlFragmentParser(root, f)(updated).parseFragment()
      case Some(header)          => parseSpecificVersion(root, updated, header)
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(spec.id)
    }
  }

  protected def context(wrapped: ParserContext,
                        root: Root,
                        options: ParsingOptions,
                        ds: Option[WebApiDeclarations] = None): RamlWebApiContext

  protected def parseSpecificVersion(root: Root, ctx: RamlWebApiContext, header: RamlHeader): BaseUnit

  private def validateReferences(references: Seq[ParsedReference], ctx: ParserContext): Unit = references.foreach {
    ref =>
      validateJsonPointersToFragments(ref, ctx)
      validateReferencesToLibraries(ref, ctx)
  }

  private def validateJsonPointersToFragments(reference: ParsedReference, ctx: ParserContext): Unit = {
    reference.unit.sourceSpec match {
      case Some(v) if v.isRaml =>
        reference.origin.refs.filter(_.uriFragment.isDefined).foreach { r =>
          ctx.eh.violation(InvalidFragmentRef, "", "Cannot use reference with # in a RAML fragment", r.node)
        }
      case _ => // Nothing to do
    }
  }

  private def validateReferencesToLibraries(reference: ParsedReference, ctx: ParserContext): Unit = {
    val refs: Seq[RefContainer] = reference.origin.refs
    val allKinds                = refs.map(_.linkType)
    val definedKind             = if (allKinds.distinct.size > 1) UnspecifiedReference else allKinds.head
    val nodes                   = refs.map(_.node)
    reference.unit match {
      case _: Module => // if is a library, kind should be LibraryReference
        if (allKinds.contains(LibraryReference) && allKinds.contains(LinkReference))
          nodes.foreach(
            ctx.eh
              .violation(ExpectedModule,
                         reference.unit.id,
                         "The !include tag must be avoided when referencing a library",
                         _))
        else if (!LibraryReference.eq(definedKind))
          nodes.foreach(
            ctx.eh.violation(ExpectedModule, reference.unit.id, "Libraries must be applied by using 'uses'", _))
      case _ =>
        // if is not a library, kind should not be LibraryReference
        if (LibraryReference.eq(definedKind))
          nodes.foreach(
            ctx.eh.violation(InvalidInclude, reference.unit.id, "Fragments must be imported by using '!include'", _))
    }
  }

  private def inlineExternalReferences(root: Root, ctx: ParserContext): Unit = {
    root.references.foreach { ref =>
      ref.unit match {
        case e: ExternalFragment =>
          inlineFragment(ref.origin.refs, ref.ast, e.encodes, ref.unit.references, ctx)
        case _ =>
      }
    }
  }

  private def inlineFragment(origins: Seq[RefContainer],
                             document: Option[YNode],
                             encodes: ExternalDomainElement,
                             elementRef: Seq[BaseUnit],
                             ctx: ParserContext): Unit = {
    origins.foreach { refContainer =>
      refContainer.node match {
        case mut: MutRef =>
          elementRef.foreach(u => ctx.addSonRef(u))
          document match {
            case None => mut.target = Some(YNode(encodes.raw.value()))
            case _    => mut.target = document
          }
        case _ =>
      }
    }
  }

  override def validSpecsToReference: Seq[Spec] = super.validSpecsToReference :+ spec
}
