package amf.apicontract.internal.spec.common.transformation

import amf.apicontract.client.scala.model.document.{ResourceTypeFragment, TraitFragment}
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.spec.common.WebApiDeclarations.ErrorEndPoint
import amf.apicontract.internal.spec.common.parser.WebApiContext
import amf.apicontract.internal.spec.raml.parser.context.{Raml08WebApiContext, Raml10WebApiContext, RamlWebApiContext}
import amf.apicontract.internal.validation.definitions.ResolutionSideValidations.ParseResourceTypeFail
import amf.core.client.common.validation.{ProfileName, Raml08Profile}
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.client.scala.model.document.{BaseUnit, DeclaresModel, Fragment, Module}
import amf.core.client.scala.model.domain.{AmfElement, DataNode, DomainElement, NamedDomainElement}
import amf.core.client.scala.parse.document.ParserContext
import amf.core.client.scala.transform.stages.ReferenceResolutionStage
import amf.core.client.scala.transform.stages.helpers.ResolvedNamedEntity
import amf.core.internal.annotations.{Aliases, LexicalInformation, SourceAST, SourceLocation}
import amf.core.internal.parser.{LimitedParseConfig, CompilerConfiguration}
import amf.core.internal.parser.domain.{Annotations, FragmentRef}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.validation.CoreValidations
import amf.shapes.internal.spec.RamlWebApiContextType
import amf.shapes.internal.spec.common.emitter.DataNodeEmitter
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class ExtendsHelper(profile: ProfileName,
                         keepEditingInfo: Boolean,
                         errorHandler: AMFErrorHandler,
                         context: Option[RamlWebApiContext] = None) {
  def custom(profile: ProfileName): RamlWebApiContext = profile match {
    case Raml08Profile => new CustomRaml08WebApiContext()
    case _             => new CustomRaml10WebApiContext()
  }

  def asOperation[T <: BaseUnit](node: DataNode,
                                 unit: T,
                                 name: String,
                                 trAnnotations: Annotations,
                                 extensionId: String): Operation = {
    val ctx = context.getOrElse(custom(profile))

    val referencesCollector = mutable.Map[String, DomainElement]()
    val entry               = emitDataNode(node, trAnnotations, name, referencesCollector)(ctx)

    context.map(_.nodeRefIds ++= ctx.nodeRefIds)
    entryAsOperation(unit, name, extensionId, entry, referencesCollector)
  }

  def parseOperation[T <: BaseUnit](unit: T,
                                    name: String,
                                    extensionId: String,
                                    entry: YMapEntry,
                                    referencesCollector: mutable.Map[String, DomainElement] =
                                      mutable.Map[String, DomainElement]()): Operation = {
    val ctx = context.getOrElse(custom(profile))

    extractContextDeclarationsFrom(unit, entry.value.sourceName)(ctx)
    referencesCollector.foreach {
      case (alias, ref) => ctx.declarations.fragments += (alias -> FragmentRef(ref, None))
    }

    val operation: Operation = {
      // we don't emit validation here, final result will be validated after merging
      ctx.adapt(name) { ctxForTrait =>
        (ctxForTrait.declarations.resourceTypes ++ ctxForTrait.declarations.traits).foreach { e =>
          ctx.declarations += e._2
        }
        ctxForTrait.nodeRefIds ++= ctx.nodeRefIds
        ctxForTrait.contextType = RamlWebApiContextType.TRAIT
        val operation = ctxForTrait.factory
          .operationParser(entry, extensionId + "/applied", true)
          .parse()
        operation
      }
    }
    operation
  }

  def entryAsOperation[T <: BaseUnit](unit: T,
                                      name: String,
                                      extensionId: String,
                                      entry: YMapEntry,
                                      referencesCollector: mutable.Map[String, DomainElement] =
                                        mutable.Map[String, DomainElement]()): Operation = {

    val operation = parseOperation(unit, name, extensionId, entry, referencesCollector)
    new ReferenceResolutionStage(keepEditingInfo).resolveDomainElement(operation, errorHandler)
  }

  def asEndpoint[T <: BaseUnit](unit: T,
                                node: DataNode,
                                rtAnnotations: Annotations,
                                name: String,
                                extensionId: String): EndPoint = {

    val ctx = context.getOrElse(custom(profile))

    val referencesCollector = mutable.Map[String, DomainElement]()
    val entry: YMapEntry    = emitDataNode(node, rtAnnotations, name, referencesCollector)(ctx)

    context.map(_.nodeRefIds ++= ctx.nodeRefIds)
    entryAsEndpoint(unit, node, name, extensionId, entry, referencesCollector)
  }

  private def emitDataNode[T <: BaseUnit](
      node: DataNode,
      rtAnnotations: Annotations,
      name: String,
      referencesCollector: mutable.Map[String, DomainElement])(implicit ctx: WebApiContext) = {
    val refIds: mutable.Map[YNode, String] = mutable.Map.empty
    val document = YDocument(
      {
        _.obj {
          _.entry(
            YNode(
              YScalar.withLocation(
                name,
                YType.Str,
                rtAnnotations
                  .find(classOf[SourceAST])
                  .map(_.ast)
                  .collectFirst({ case e: YMapEntry => Annotations(e.key) })
                  .getOrElse(rtAnnotations)
                  .sourceLocation
              ),
              YType.Str
            ),
            DataNodeEmitter(node, getSpecOrderingFrom(rtAnnotations), referencesCollector)(ctx.eh, refIds)
              .emit(_)
          )
        }
      },
      node.location().getOrElse("")
    )
    ctx.nodeRefIds ++= refIds
    document.as[YMap].entries.head
  }

  private def getSpecOrderingFrom[T <: BaseUnit](rtAnnotations: Annotations) = {
    if (rtAnnotations.contains(classOf[LexicalInformation])) SpecOrdering.Lexical
    else SpecOrdering.Default
  }

  def entryAsEndpoint[T <: BaseUnit](unit: T,
                                     node: DataNode,
                                     name: String,
                                     extensionId: String,
                                     entry: YMapEntry,
                                     referencesCollector: mutable.Map[String, DomainElement] =
                                       mutable.Map[String, DomainElement]()): EndPoint = {
    val ctx       = context.getOrElse(custom(profile))
    val collector = ListBuffer[EndPoint]()

    extractContextDeclarationsFrom(unit, entry.sourceName)(ctx)
    referencesCollector.foreach {
      case (alias, ref) => ctx.declarations.fragments += (alias -> FragmentRef(ref, None))
    }

    ctx.adapt(name) { ctxForResourceType =>
      (ctxForResourceType.declarations.resourceTypes ++ ctxForResourceType.declarations.traits).foreach { e =>
        ctx.declarations += e._2
      }
      ctxForResourceType.nodeRefIds ++= ctx.nodeRefIds
      ctxForResourceType.contextType = RamlWebApiContextType.RESOURCE_TYPE
      ctxForResourceType.factory
        .endPointParser(entry, _ => EndPoint().withId(extensionId + "/applied"), None, collector, true)
        .parse()
      ctx.operationContexts ++= ctxForResourceType.operationContexts
    }

    collector.toList match {
      case element :: _ =>
        new ReferenceResolutionStage(keepEditingInfo).resolveDomainElement(element, errorHandler)
      case Nil =>
        errorHandler.violation(
          ParseResourceTypeFail,
          extensionId,
          None,
          s"Couldn't parse an endpoint from resourceType '$name'.",
          node.position(),
          node.location()
        )
        ErrorEndPoint(node.id, entry)
    }
  }

  private def getDeclaringUnit(refs: List[BaseUnit], sourceName: String): Option[BaseUnit] = refs match {
    case (f: Fragment) :: _ if sourceNameMatch(f, sourceName) => Some(f)
    case (m: Module) :: _ if sourceNameMatch(m, sourceName)   => Some(m)
    case unit :: tail =>
      getDeclaringUnit(tail, sourceName) match {
        case ref @ Some(_) => ref
        case _             => getDeclaringUnit(unit.references.toList, sourceName)
      }
    case _ => None
  }

  private def sourceNameMatch(f: AmfElement, sourceName: String): Boolean =
    f.annotations
      .find(classOf[SourceLocation])
      .map(_.location)
      .contains(sourceName)

  private def extractFilteredDeclarations(unit: BaseUnit,
                                          filterCondition: DomainElement => Boolean): Seq[DomainElement] = {
    unit match {
      case d: DeclaresModel => d.declares.filter(filterCondition)
      case _                => Nil
    }
  }

  /** Extract declarations with root and local names from the root API base unit and the base unit declaring the
    *  RT/Trait, identified by its source name.
    * @param root root API base unit
    * @param sourceName source name of the base unit declaring the RT/Trait
    * @param ctx context to populate with declarations
    */
  private def extractContextDeclarationsFrom(root: BaseUnit, sourceName: String)(ctx: RamlWebApiContext): Unit = {
    val declaringUnit     = getDeclaringUnit(root.references.toList, sourceName).getOrElse(root)
    val libraries         = extractFilteredDeclarations(declaringUnit, _.isInstanceOf[Module]).map((_, declaringUnit))
    val otherDeclarations = extractFilteredDeclarations(root, !_.isInstanceOf[Module]).map((_, root))

    libraries ++ otherDeclarations foreach {
      case (declaration, unit) =>
        ctx.declarations += declaration
        extractDeclarationToContextWithLocalAndRootName(declaration, unit)(ctx)
    }

    // gives priority to references of fragments or modules over those in root file, this order can be adjusted
    if (declaringUnit != root) extractContextDeclarationsFromReferencesIn(root)(ctx)
    extractContextDeclarationsFromReferencesIn(declaringUnit)(ctx)
  }

  private def extractContextDeclarationsFromReferencesIn(model: BaseUnit)(ctx: RamlWebApiContext): Unit = {
    model.references.foreach {
      // Declarations of traits and resourceTypes are contextual, so should skip it
      case f: Fragment if !f.isInstanceOf[ResourceTypeFragment] && !f.isInstanceOf[TraitFragment] =>
        ctx.declarations += (f.location().getOrElse(f.id), f)
      case m: DeclaresModel =>
        model.annotations.find(classOf[Aliases]).getOrElse(Aliases(Set())).aliases.foreach {
          case (alias, (fullUrl, _)) if m.id == fullUrl =>
            val nestedCtx = new Raml10WebApiContext("", Nil, ParserContext(config = LimitedParseConfig(ctx.eh)))
            m.declares.foreach { declaration =>
              extractDeclarationToContextWithLocalAndRootName(declaration, m)(nestedCtx)
            }
            ctx.declarations.libraries += (alias -> nestedCtx.declarations)
          case _ => // Ignore
        }
      case _: Fragment => // Trait or RT, nothing to do
      case other =>
        ctx.eh.violation(
          CoreValidations.ResolutionValidation,
          other,
          None,
          "Error resolving nested declaration, found something that is not a library or a fragment"
        )
        other
    }
  }

  private def extractDeclarationToContextWithLocalAndRootName(declaration: DomainElement, local: BaseUnit)(
      ctx: RamlWebApiContext): Unit = {
    declaration.annotations.find(classOf[ResolvedNamedEntity]) match {
      case Some(resolvedNamedEntity) =>
        resolvedNamedEntity.vals.foreach {
          case (_, localDeclarations) =>
            ctx.declarations += declaration
            declaration match {
              // we recover the local alias we removed when resolving
              case element: NamedDomainElement =>
                for {
                  location         <- local.location()
                  localDeclaration <- localDeclarations.find(e => e.location().contains(location))
                  localName        <- localDeclaration.name.option()
                  realName         <- element.name.option()
                } yield {
                  element.withName(localName)
                  ctx.declarations += declaration
                  element.withName(realName) // This is useless?
                }
              case _ =>
            }
        }
      case _ => ctx.declarations += declaration
    }
  }
}

object ExtendsHelper {
  def findUnitLocationOfElement(elementId: String, unit: BaseUnit): Option[String] = {
    unit.references.collectFirst({
      case l: Module if l.declares.exists(_.id == elementId) => l.location().getOrElse(l.id)
      case f: Fragment if f.encodes.id == elementId          => f.location().getOrElse(f.id)
    })
  }
}

class CustomRaml08WebApiContext
    extends Raml08WebApiContext("", Nil, ParserContext(config = LimitedParseConfig(IgnoringErrorHandler)))
class CustomRaml10WebApiContext
    extends Raml10WebApiContext("", Nil, ParserContext(config = LimitedParseConfig(IgnoringErrorHandler)))
