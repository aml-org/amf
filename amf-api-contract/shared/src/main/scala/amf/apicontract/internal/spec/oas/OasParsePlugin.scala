package amf.apicontract.internal.spec.oas

import amf.apicontract.internal.spec.common.OasWebApiDeclarations
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.oas.parser.document.{OasFragmentParser, OasModuleParser}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.{LibraryReference, LinkReference, ParsedReference, ParserContext}
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec

trait OasParsePlugin extends OasLikeParsePlugin {

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    implicit val newCtx: OasWebApiContext = context(document.location, document.references, ctx.parsingOptions, ctx)
    restrictCrossSpecReferences(document, ctx)
    val parsed = document.referenceKind match {
      case LibraryReference => OasModuleParser(document).parseModule()
      case LinkReference    => OasFragmentParser(document).parseFragment()
      case _                => parseSpecificVersion(document)
    }
    promoteFragments(parsed, newCtx)
  }

  protected def parseSpecificVersion(root: Root)(implicit ctx: OasWebApiContext): BaseUnit

  protected def context(loc: String,
                        refs: Seq[ParsedReference],
                        options: ParsingOptions,
                        wrapped: ParserContext,
                        ds: Option[OasWebApiDeclarations] = None): OasWebApiContext

  // TODO ARM: Decide, should an OAS Document be able to reference another OAS Document?
  override def validSpecsToReference: Seq[Spec] = super.validSpecsToReference :+ spec
}
