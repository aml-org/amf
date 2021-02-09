package amf.plugins.domain.webapi.resolution

import amf.client.parse.{DefaultParserErrorHandler, IgnoringErrorHandler}
import amf.core.annotations.{Aliases, LexicalInformation, SourceAST, SourceLocation => AmfSourceLocation}
import amf.core.emitter.SpecOrdering
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, DeclaresModel, Fragment, Module}
import amf.core.model.domain._
import amf.core.parser.{Annotations, FragmentRef, ParserContext}
import amf.core.resolution.stages.ReferenceResolutionStage
import amf.core.resolution.stages.helpers.ResolvedNamedEntity
import amf.core.validation.core.ValidationSpecification
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.contexts.parser.raml.{
  Raml08WebApiContext,
  Raml10WebApiContext,
  RamlWebApiContext,
  RamlWebApiContextType
}
import amf.plugins.document.webapi.model.{ResourceTypeFragment, TraitFragment}
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorEndPoint
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.DataNodeEmitter
import amf.plugins.domain.webapi.models.{EndPoint, Operation}
import amf.plugins.features.validation.CoreValidations
import amf.validations.ResolutionSideValidations.ParseResourceTypeFail
import amf.{ProfileName, Raml08Profile}
import org.mulesoft.lexer.SourceLocation
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object ExtendsHelper {
  def custom(profile: ProfileName): RamlWebApiContext = profile match {
    case Raml08Profile => new CustomRaml08WebApiContext()
    case _             => new CustomRaml10WebApiContext()
  }

  def asOperation[T <: BaseUnit](profile: ProfileName,
                                 node: DataNode,
                                 unit: T,
                                 name: String,
                                 trAnnotations: Annotations,
                                 extensionId: String,
                                 extensionLocation: Option[String],
                                 keepEditingInfo: Boolean,
                                 context: Option[RamlWebApiContext] = None,
                                 errorHandler: ErrorHandler): Operation = {
    val ctx = context.getOrElse(custom(profile))

    val referencesCollector = mutable.Map[String, DomainElement]()
    val entry               = emitDataNode(node, trAnnotations, name, referencesCollector)(ctx)

    context.map(_.nodeRefIds ++= ctx.nodeRefIds)
    entryAsOperation(profile,
                     unit,
                     name,
                     extensionId,
                     keepEditingInfo,
                     entry,
                     node.annotations,
                     referencesCollector,
                     context,
                     errorHandler)
  }

  def entryAsOperation[T <: BaseUnit](profile: ProfileName,
                                      unit: T,
                                      name: String,
                                      extensionId: String,
                                      keepEditingInfo: Boolean,
                                      entry: YMapEntry,
                                      annotations: Annotations,
                                      referencesCollector: mutable.Map[String, DomainElement] =
                                        mutable.Map[String, DomainElement](),
                                      context: Option[RamlWebApiContext] = None,
                                      errorHandler: ErrorHandler): Operation = {
    val ctx = context.getOrElse(custom(profile))

    addDeclarations(ctx, unit, entry.value.sourceName)
    referencesCollector.foreach {
      case (alias, ref) => ctx.declarations.fragments += (alias -> FragmentRef(ref, None))
    }

    val operation: Operation =
      // we don't emit validation here, final result will be validated after merging
      ctx.adapt(name) { ctxForTrait =>
        (ctxForTrait.declarations.resourceTypes ++ ctxForTrait.declarations.traits).foreach { e =>
          ctx.declarations += e._2
        }
        ctxForTrait.nodeRefIds ++= ctx.nodeRefIds
        ctxForTrait.contextType = RamlWebApiContextType.TRAIT
        val operation = ctxForTrait.factory
          .operationParser(entry, _ => Operation().withId(extensionId + "/applied"), true)
          .parse()
        operation
      }

    new ReferenceResolutionStage(keepEditingInfo)(errorHandler).resolveDomainElement(operation)
  }

  def asEndpoint[T <: BaseUnit](unit: T,
                                profile: ProfileName,
                                node: DataNode,
                                rtAnnotations: Annotations,
                                name: String,
                                extensionId: String,
                                extensionLocation: Option[String],
                                keepEditingInfo: Boolean,
                                context: Option[RamlWebApiContext] = None,
                                errorHandler: ErrorHandler): EndPoint = {

    val ctx = context.getOrElse(custom(profile))

    val referencesCollector = mutable.Map[String, DomainElement]()
    val entry: YMapEntry    = emitDataNode(node, rtAnnotations, name, referencesCollector)(ctx)

    context.map(_.nodeRefIds ++= ctx.nodeRefIds)
    entryAsEndpoint(profile,
                    unit,
                    node,
                    name,
                    extensionId,
                    keepEditingInfo,
                    entry,
                    node.annotations,
                    errorHandler,
                    extensionLocation,
                    referencesCollector,
                    context)
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
            DataNodeEmitter(node, getSpecOrderingFrom(rtAnnotations), referencesCollector)(ctx.eh, refIds).emit(_)
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

  def entryAsEndpoint[T <: BaseUnit](profile: ProfileName,
                                     unit: T,
                                     node: DataNode,
                                     name: String,
                                     extensionId: String,
                                     keepEditingInfo: Boolean,
                                     entry: YMapEntry,
                                     annotations: Annotations,
                                     errorHandler: ErrorHandler,
                                     extensionLocation: Option[String],
                                     referencesCollector: mutable.Map[String, DomainElement] =
                                       mutable.Map[String, DomainElement](),
                                     context: Option[RamlWebApiContext] = None): EndPoint = {
    val ctx       = context.getOrElse(custom(profile))
    val collector = ListBuffer[EndPoint]()

    addDeclarations(ctx, unit, entry.sourceName)
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
        new ReferenceResolutionStage(keepEditingInfo)(errorHandler).resolveDomainElement(element)
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

  def findUnitLocationOfElement(elementId: String, unit: BaseUnit): Option[String] = {

    unit.references.collectFirst({
      case l: Module if l.declares.exists(_.id == elementId) => l.location().getOrElse(l.id)
      case f: Fragment if f.encodes.id == elementId          => f.location().getOrElse(f.id)
    })
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
      .find(classOf[AmfSourceLocation])
      .map(_.location)
      .contains(sourceName)

  private def extractFilteredDeclarations(unit: BaseUnit,
                                          filterCondition: DomainElement => Boolean): Seq[DomainElement] = {
    unit match {
      case d: DeclaresModel => d.declares.filter(filterCondition)
      case _                => Nil
    }
  }

  private def addDeclarations(ctx: RamlWebApiContext, root: BaseUnit, sourceName: String): Unit = {
    val declaringUnit     = getDeclaringUnit(root.references.toList, sourceName).getOrElse(root)
    val libraries         = extractFilteredDeclarations(declaringUnit, _.isInstanceOf[Module]).map((_, declaringUnit))
    val otherDeclarations = extractFilteredDeclarations(root, !_.isInstanceOf[Module]).map((_, root))

    libraries ++ otherDeclarations foreach {
      case (declaration, unit) =>
        ctx.declarations += declaration
        processDeclaration(declaration, ctx, unit)
    }

    // gives priority to references of fragments or modules over those in root file, this order can be adjusted
    if (declaringUnit != root) processRefsToDeclarations(ctx, root)
    processRefsToDeclarations(ctx, declaringUnit)
  }

  private def processRefsToDeclarations(ctx: RamlWebApiContext, model: BaseUnit): Unit = {
    model.references.foreach {
      // Declarations of traits and resourceTypes are contextual, so should skip it
      case f: Fragment if !f.isInstanceOf[ResourceTypeFragment] && !f.isInstanceOf[TraitFragment] =>
        ctx.declarations += (f.location().getOrElse(f.id), f)
      case m: DeclaresModel =>
        model.annotations.find(classOf[Aliases]).getOrElse(Aliases(Set())).aliases.foreach {
          case (alias, (fullUrl, _)) =>
            if (m.id == fullUrl) {
              val nestedCtx = new Raml10WebApiContext("", Nil, ParserContext(eh = ctx.eh))
              m.declares.foreach { declaration =>
                processDeclaration(declaration, nestedCtx, m)
              }
              ctx.declarations.libraries += (alias -> nestedCtx.declarations)
            }
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

  private def processDeclaration(declaration: DomainElement, nestedCtx: RamlWebApiContext, model: BaseUnit): Unit = {
    declaration.annotations.find(classOf[ResolvedNamedEntity]) match {
      case Some(resolvedNamedEntity) =>
        resolvedNamedEntity.vals.foreach {
          case (_, namedEntities) =>
            val inContext = namedEntities.find(
              entity =>
                entity.isInstanceOf[DomainElement] && entity
                  .asInstanceOf[DomainElement]
                  .id
                  .contains(model.location().getOrElse("")))
            nestedCtx.declarations += declaration
            declaration match {
              // we recover the local alias we removed when resolving
              case element: NamedDomainElement if inContext.isDefined =>
                val localName = inContext.get.name.value()
                val realName  = element.name.value()
                element.withName(localName)
                nestedCtx.declarations += declaration
                element.withName(realName)
              case _ =>
            }
        }
      case _ => nestedCtx.declarations += declaration
    }
  }
}

class CustomRaml08WebApiContext extends Raml08WebApiContext("", Nil, ParserContext(eh = IgnoringErrorHandler())) { // generating a new id???? cannot be ok
}

class CustomRaml10WebApiContext extends Raml10WebApiContext("", Nil, ParserContext(eh = IgnoringErrorHandler())) {}
