package amf.plugins.domain.webapi.resolution

import amf.core.annotations._
import amf.core.emitter.BaseEmitters.yscalarWithRange
import amf.core.emitter.SpecOrdering
import amf.core.model.document.{BaseUnit, DeclaresModel, Fragment, Module}
import amf.core.model.domain._
import amf.core.parser.{Annotations, ErrorHandler, FragmentRef, KnownContextVariables, ParserContext}
import amf.core.resolution.stages.{ReferenceResolutionStage, ResolvedNamedEntity}
import amf.core.services.RuntimeValidator
import amf.core.validation.core.ValidationSpecification
import amf.plugins.document.webapi.annotations.ExtensionProvenance
import amf.plugins.document.webapi.contexts.{Raml08WebApiContext, Raml10WebApiContext, RamlWebApiContext}
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.ErrorEndPoint
import amf.plugins.document.webapi.parser.spec.declaration.DataNodeEmitter
import amf.plugins.domain.webapi.models.{EndPoint, Operation}
import amf.plugins.features.validation.ResolutionSideValidations.{
  NestedEndpoint,
  ParseResourceTypeFail,
  ResolutionValidation
}
import amf.{ProfileName, Raml08Profile}
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
                                 context: Option[RamlWebApiContext] = None): Operation = {
    val ctx = context.getOrElse(custom(profile))

    val referencesCollector = mutable.Map[String, DomainElement]()
    val document = YDocument(
      {
        _.obj {
          _.entry(
            YNode(
              yscalarWithRange(name,
                               YType.Str,
                               trAnnotations
                                 .find(classOf[SourceAST])
                                 .map(_.ast)
                                 .collectFirst({ case e: YMapEntry => Annotations(e.key) })
                                 .getOrElse(trAnnotations)),
              YType.Str
            ),
            DataNodeEmitter(node,
                            if (trAnnotations.contains(classOf[LexicalInformation])) SpecOrdering.Lexical
                            else SpecOrdering.Default,
                            resolvedLinks = true,
                            referencesCollector)(ctx).emit(_)
          )
        }
      },
      node.location().getOrElse("")
    )

    val entry = document.as[YMap].entries.head

    entryAsOperation(profile,
                     unit,
                     name,
                     extensionId,
                     keepEditingInfo,
                     entry,
                     node.annotations,
                     referencesCollector,
                     context)
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
                                      context: Option[RamlWebApiContext] = None): Operation = {
    val ctx = context.getOrElse(custom(profile))

    declarations(ctx, unit)
    referencesCollector.foreach {
      case (alias, ref) => ctx.declarations.fragments += (alias -> FragmentRef(ref, None))
    }

    val operation: Operation =
      RuntimeValidator.nestedValidation(ResourceTypeAndTraitValidationsMerger(ctx.parserCount)) { // we don't emit validation here, final result will be validated after merging
        ctx.adapt(name) { ctxForTrait =>
          (ctxForTrait.declarations.resourceTypes ++ ctxForTrait.declarations.traits).foreach { e =>
            ctx.declarations += e._2
          }
          ctxForTrait.variables += (KnownContextVariables.TRAIT_CONTEXT, true)
          val operation = ctxForTrait.factory
            .operationParser(entry, _ => Operation().withId(extensionId + "/applied"), true)
            .parse()
//          ctxForTrait.futureDeclarations.resolve()
          operation
        }
      }
    checkNoNestedEndpoints(entry, ctx, annotations, extensionId, "trait")

    if (keepEditingInfo) annotateExtensionId(operation, extensionId, findUnitLocationOfElement(extensionId, unit))
    operation
    // new ReferenceResolutionStage(profile, keepEditingInfo).resolveDomainElement(operation)
  }

  private def checkNoNestedEndpoints(entry: YMapEntry,
                                     ctx: RamlWebApiContext,
                                     annotations: Annotations,
                                     extensionId: String,
                                     extension: String): Unit = {
    entry.value.tagType match {
      case YType.Map =>
        entry.value.as[YMap].map.keySet.foreach { propertyNode =>
          val property = propertyNode.as[YScalar].text
          if (property.startsWith("/")) {
            ctx.violation(
              NestedEndpoint,
              extensionId,
              None,
              s"Nested endpoint in $extension: '$property'",
              annotations.find(classOf[LexicalInformation]),
              annotations.find(classOf[SourceLocation]).map(_.location)
            )
          }
        }
      case _ => // ignore
    }
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
    val document = YDocument(
      {
        _.obj {
          _.entry(
            YNode(
              yscalarWithRange(name,
                               YType.Str,
                               rtAnnotations
                                 .find(classOf[SourceAST])
                                 .map(_.ast)
                                 .collectFirst({ case e: YMapEntry => Annotations(e.key) })
                                 .getOrElse(rtAnnotations)),
              YType.Str
            ),
            DataNodeEmitter(node,
                            if (rtAnnotations.contains(classOf[LexicalInformation])) SpecOrdering.Lexical
                            else SpecOrdering.Default,
                            resolvedLinks = true,
                            referencesCollector)(ctx).emit(_)
          )
        }
      },
      node.location().getOrElse("")
    )

    val entry = document.as[YMap].entries.head

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

    declarations(ctx, unit)
    referencesCollector.foreach {
      case (alias, ref) => ctx.declarations.fragments += (alias -> FragmentRef(ref, None))
    }

    RuntimeValidator.nestedValidation(ResourceTypeAndTraitValidationsMerger(ctx.parserCount)) { // we don't emit validation here, final result will be validated after mergin
      ctx.adapt(name) { ctxForResourceType =>
        (ctxForResourceType.declarations.resourceTypes ++ ctxForResourceType.declarations.traits).foreach { e =>
          ctx.declarations += e._2
        }
        ctxForResourceType.variables.+=(KnownContextVariables.RESOURCE_TYPE_CONTEXT, true)
        ctxForResourceType.factory
          .endPointParser(entry, _ => EndPoint().withId(extensionId + "/applied"), None, collector, true)
          .parse()
//        ctxForResourceType.futureDeclarations.resolve()
      }
    }

    checkNoNestedEndpoints(entry, ctx, node.annotations, extensionId, "resourceType")

    collector.toList match {
      case element :: _ =>
        if (keepEditingInfo) annotateExtensionId(element, extensionId, extensionLocation)
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

  private def annotateExtensionId(point: DomainElement, extensionId: String, extensionLocation: Option[String]): Unit = {
    val annotation = ExtensionProvenance(extensionId, extensionLocation)
    if (!point.fields
          .fields()
          .headOption
          .exists(_.value.annotations.collect({ case e: ExtensionProvenance => e }).exists(_.baseId == extensionId))) {
      point.fields.fields().foreach { field =>
        field.value.annotations += annotation
        field.value.value match {
          case elem: DomainElement => annotateExtensionId(elem, extensionId, extensionLocation)
          case arr: AmfArray =>
            arr.values.foreach {
              case elem: DomainElement =>
                elem.annotations += annotation
                annotateExtensionId(elem, extensionId, extensionLocation)
              case other =>
                other.annotations += annotation
            }
          case scalar => scalar.annotations += annotation
        }
      }
    }
  }

  private def declarations(ctx: RamlWebApiContext, model: BaseUnit): Unit = {
    model match {
      case d: DeclaresModel =>
        d.declares.foreach { declaration =>
          ctx.declarations += declaration
          processDeclaration(declaration, ctx, model)
        }
      case _ =>
    }
    nestedDeclarations(ctx, model)
  }

  private def nestedDeclarations(ctx: RamlWebApiContext, model: BaseUnit): Unit = {
    model.references.foreach {
      case f: Fragment =>
        ctx.declarations += (f.location().getOrElse(f.id), f)
        nestedDeclarations(ctx, f)
      case m: DeclaresModel =>
        model.annotations.find(classOf[Aliases]).getOrElse(Aliases(Set())).aliases.foreach {
          case (alias, (fullUrl, _)) =>
            // If the library alias is already in the context, skip it
            if (m.id == fullUrl && !ctx.declarations.libraries.exists(_._1 == alias)) {
              val nestedCtx = new Raml10WebApiContext("", Nil, ParserContext(), eh = ctx.eh)
              m.declares.foreach { declaration =>
                processDeclaration(declaration, nestedCtx, m)
              }
              ctx.declarations.libraries += (alias -> nestedCtx.declarations)
            }
        }
        nestedDeclarations(ctx, m)
      case other =>
        ctx.violation(
          ResolutionValidation,
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

class CustomRaml08WebApiContext extends Raml08WebApiContext("", Nil, ParserContext()) {
  override def handle[T](error: YError, defaultValue: T): T = defaultValue
  override def violation(id: ValidationSpecification,
                         node: String,
                         property: Option[String],
                         message: String,
                         lexical: Option[LexicalInformation],
                         location: Option[String]): Unit =
    super.violation(id, node, property, message, lexical, location)
  override def warning(id: ValidationSpecification,
                       node: String,
                       property: Option[String],
                       message: String,
                       lexical: Option[LexicalInformation],
                       location: Option[String]): Unit      = {}
  override def handle(node: YPart, e: SyamlException): Unit = {}
}

class CustomRaml10WebApiContext extends Raml10WebApiContext("", Nil, ParserContext()) {
  override def handle[T](error: YError, defaultValue: T): T = defaultValue
  override def violation(id: ValidationSpecification,
                         node: String,
                         property: Option[String],
                         message: String,
                         lexical: Option[LexicalInformation],
                         location: Option[String]): Unit =
    super.violation(id, node, property, message, lexical, location)
  override def warning(id: ValidationSpecification,
                       node: String,
                       property: Option[String],
                       message: String,
                       lexical: Option[LexicalInformation],
                       location: Option[String]): Unit      = {}
  override def handle(node: YPart, e: SyamlException): Unit = {}
}
