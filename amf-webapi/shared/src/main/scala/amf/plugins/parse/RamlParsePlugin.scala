package amf.plugins.parse
import amf.client.remod.amfcore.config.ParsingOptions
import amf.core.Root
import amf.core.errorhandling.AMFErrorHandler
import amf.core.exception.InvalidDocumentHeaderException
import amf.core.model.document.{BaseUnit, ExternalFragment, Module}
import amf.core.model.domain.ExternalDomainElement
import amf.core.parser.{
  EmptyFutureDeclarations,
  LibraryReference,
  LinkReference,
  ParsedReference,
  ParserContext,
  RefContainer,
  ReferenceHandler,
  UnspecifiedReference
}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.parser.RamlHeader.{Raml08, Raml10, Raml10Extension, Raml10Library, Raml10Overlay}
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations
import amf.plugins.document.webapi.parser.spec.raml._
import amf.plugins.document.webapi.parser.{RamlFragment, RamlHeader}
import amf.plugins.document.webapi.references.RamlReferenceHandler
import amf.plugins.features.validation.CoreValidations.{ExpectedModule, InvalidFragmentRef, InvalidInclude}
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
      case Some(f: RamlFragment) => RamlFragmentParser(root, f)(updated).parseFragment()
      case Some(header)          => parseSpecificVersion(root, updated, header)
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(vendor.name)
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
    reference.unit.sourceVendor match {
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
    val definedKind             = if (allKinds.size > 1) UnspecifiedReference else allKinds.head
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
}
