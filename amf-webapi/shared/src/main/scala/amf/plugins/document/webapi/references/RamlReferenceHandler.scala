package amf.plugins.document.webapi.references

import amf.core.CompilerContext
import amf.core.annotations.SourceAST
import amf.core.model.document.{BaseUnit, ExternalFragment}
import amf.core.model.domain.ExternalDomainElement
import amf.core.parser.{ParsedReference, ParserContext, Reference, ReferenceResolutionResult, SyamlParsedDocument}
import amf.core.remote.{Raml, Raml08, Raml10}
import amf.plugins.document.webapi.BaseWebApiPlugin
import amf.plugins.features.validation.CoreValidations.InvalidFragmentRef
import amf.validations.ParserSideValidations.InvalidFragmentType
import org.yaml.model.{YDocument, YNode}
import org.yaml.model.YNode.MutRef
import org.yaml.parser.YamlParser
import amf.core.TaggedReferences._
import scala.concurrent.{ExecutionContext, Future}

class RamlReferenceHandler(vendor: String, plugin: BaseWebApiPlugin) extends WebApiReferenceHandler(vendor) {

  /** Update parsed reference if needed. */
  override def update(reference: ParsedReference, compilerContext: CompilerContext)(
      implicit executionContext: ExecutionContext): Future[ParsedReference] = {
    if (reference.isExternalFragment)
      handleRamlExternalFragment(reference, compilerContext)
    else Future.successful(reference)
  }

  private def handleRamlExternalFragment(reference: ParsedReference, compilerContext: CompilerContext)(
      implicit executionContext: ExecutionContext): Future[ParsedReference] = {
    resolveUnitDocument(reference, compilerContext.parserContext) match {
      case Right(document) =>
        val parsed = SyamlParsedDocument(document)

        val refs    = new RamlReferenceHandler(vendor, plugin).collect(parsed, compilerContext.parserContext)
        val updated = compilerContext.forReference(reference.unit.id, withNormalizedUri = false)

        val externals = refs.toReferences.map((r: Reference) => {
          r.resolve(updated, r.refs.map(_.node), allowRecursiveRefs = true, plugin)
            .flatMap {
              case ReferenceResolutionResult(None, Some(unit)) =>
                val resolved = handleRamlExternalFragment(ParsedReference(unit, r), updated)
                reference.unit.tagReference(unit.location().getOrElse(unit.id), r)
                resolved.map(res => {
                  reference.unit.addReference(res.unit)
                  r.refs.foreach { refContainer =>
                    refContainer.node match {
                      case mut: MutRef =>
                        res.unit.references.foreach(u => compilerContext.parserContext.addSonRef(u))
                        mut.target = res.ast
                      case other =>
                        compilerContext.violation(InvalidFragmentType,
                                                  "Cannot inline a fragment in a not mutable node",
                                                  other)
                    }
                  // not meaning, only for collect all futures, not matter the type
                  }
                })
              case ReferenceResolutionResult(Some(_), _) => Future(Nil)
              case _                                     => Future(Nil)
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

  private def resolveUnitDocument(reference: ParsedReference, ctx: ParserContext): Either[String, YDocument] = {
    reference.unit match {

      case e: ExternalFragment if isRamlOrYaml(e.encodes) =>
        Right(
          YamlParser(e.encodes.raw.value(), e.location().getOrElse(""))(ctx.eh)
            .withIncludeTag("!include")
            .document())
      case e: ExternalFragment =>
        Left(e.encodes.raw.value())
      case o if hasDocumentAST(o) =>
        Right(o.annotations.find(classOf[SourceAST]).map(_.ast.asInstanceOf[YDocument]).get)
      case _ => Left("")
    }
  }

  private def isRamlOrYaml(encodes: ExternalDomainElement) =
    plugin.documentSyntaxes.contains(encodes.mediaType.value())

  private def hasDocumentAST(other: BaseUnit) =
    other.annotations.find(classOf[SourceAST]).exists(_.ast.isInstanceOf[YDocument])
}
