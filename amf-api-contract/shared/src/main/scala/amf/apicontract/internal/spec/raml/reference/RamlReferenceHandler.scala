package amf.apicontract.internal.spec.raml.reference

import amf.apicontract.internal.spec.common.reference.ApiReferenceHandler
import amf.core.client.scala.model.document.{BaseUnit, ExternalFragment}
import amf.core.client.scala.model.domain.ExternalDomainElement
import amf.core.client.scala.parse.AMFParsePlugin
import amf.core.client.scala.parse.TaggedReferences.BuReferenceTagger
import amf.core.client.scala.parse.document._
import amf.core.internal.annotations.SourceYPart
import amf.core.internal.parser.CompilerContext
import amf.core.internal.plugins.syntax.SYamlAMFParserErrorHandler
import amf.core.internal.validation.CoreValidations.UnresolvedReference
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidFragmentType
import org.yaml.model.YNode.MutRef
import org.yaml.model.{YDocument, YNode}
import org.yaml.parser.YamlParser

import scala.concurrent.{ExecutionContext, Future}

class RamlReferenceHandler(plugin: AMFParsePlugin) extends ApiReferenceHandler(plugin.id) {

  /** Update parsed reference if needed. */
  override def update(reference: ParsedReference, compilerContext: CompilerContext)(implicit
      executionContext: ExecutionContext
  ): Future[ParsedReference] = {
    if (reference.isExternalFragment)
      handleRamlExternalFragment(reference, compilerContext)
    else Future.successful(reference)
  }

  private def handleRamlExternalFragment(reference: ParsedReference, compilerContext: CompilerContext)(implicit
      executionContext: ExecutionContext
  ): Future[ParsedReference] = {
    resolveUnitDocument(reference, compilerContext.parserContext) match {
      case Right(document) =>
        val parsed = SyamlParsedDocument(document)

        val refs         = new RamlReferenceHandler(plugin).collect(parsed, compilerContext.parserContext)
        val updated      = compilerContext.forReference(reference.origin.url)
        val allowedSpecs = plugin.validSpecsToReference :+ plugin.spec
        val externals = refs.toReferences.map((r: Reference) => {
          r.resolve(updated, allowedSpecs, allowRecursiveRefs = true) // why would this always allow recursions?
            .flatMap {
              case ReferenceResolutionResult(None, Some(unit)) =>
                val resolved = handleRamlExternalFragment(ParsedReference(unit, r), updated)
                reference.unit.tagReference(unit.location().getOrElse(unit.id), r)
                resolved.map(res => {
                  reference.unit.addReference(res.unit)
                  r.refs.foreach { case refContainer: SYamlRefContainer =>
                    refContainer.node match {
                      case mut: MutRef =>
                        res.unit.references.foreach(u => compilerContext.parserContext.addSonRef(u))
                        mut.target = res.ast
                      case other =>
                        compilerContext.violation(
                          InvalidFragmentType,
                          "Cannot inline a fragment in a not mutable node",
                          other.location
                        )
                    }
                  // not meaning, only for collect all futures, not matter the type
                  }
                })
              case ReferenceResolutionResult(Some(e), None) =>
                evaluateUnresolvedReference(compilerContext, r, e)
                Future(Nil)
              case _ => Future(Nil)
            }
        })

        Future.sequence(externals).map(_ => reference.copy(ast = Some(document.node)))
      case Left(raw) =>
        Future.successful {
          reference.unit.references.foreach(u => compilerContext.parserContext.addSonRef(u))
          reference.copy(ast = Some(YNode(raw, reference.unit.location().getOrElse(""))))
        }
    }
  }

  private def evaluateUnresolvedReference(compilerContext: CompilerContext, r: Reference, e: Throwable): Unit = {
    if (!r.isInferred) {
      r.refs.foreach { case ref: ASTRefContainer =>
        compilerContext.violation(UnresolvedReference, r.url, e.getMessage, ref.pos)
      }
    }
  }

  private def resolveUnitDocument(reference: ParsedReference, ctx: ParserContext): Either[String, YDocument] = {
    reference.unit match {

      case e: ExternalFragment if isRamlOrYaml(e.encodes) =>
        Right(
          YamlParser(e.encodes.raw.value(), e.location().getOrElse(""), ctx.parsingOptions.getMaxJsonYamlDepth)(
            new SYamlAMFParserErrorHandler(ctx.eh)
          )
            .withIncludeTag("!include")
            .document()
        )
      case e: ExternalFragment =>
        Left(e.encodes.raw.value())
      case o if hasDocumentAST(o) =>
        Right(o.annotations.find(classOf[SourceYPart]).map(_.ast.asInstanceOf[YDocument]).get)
      case _ => Left("")
    }
  }

  private def isRamlOrYaml(encodes: ExternalDomainElement) =
    encodes.mediaType.option().exists(_.contains("yaml"))

  private def hasDocumentAST(other: BaseUnit) =
    other.annotations.find(classOf[SourceYPart]).exists(_.ast.isInstanceOf[YDocument])
}
