package amf.plugins.document.webapi.resolution.stages

import amf.ProfileNames
import amf.core.annotations.{LexicalInformation, SourceAST}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{DataNode, DomainElement}
import amf.core.parser.ParserContext
import amf.core.resolution.stages.ResolutionStage
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.contexts.{Raml08WebApiContext, Raml10WebApiContext, RamlWebApiContext}
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations.{ErrorDeclaration, ErrorEndPoint}
import amf.plugins.domain.webapi.models.templates.{ParametrizedResourceType, ParametrizedTrait, ResourceType, Trait}
import amf.plugins.domain.webapi.models.{EndPoint, Operation}
import amf.plugins.domain.webapi.resolution.ExtendsHelper
import amf.plugins.domain.webapi.resolution.stages.DomainElementMerging
import amf.plugins.domain.webapi.resolution.stages.DomainElementMerging._
import org.yaml.model.YNode

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * 1) Get a queue of resource types for this Endpoint.
  * 2) Resolve each resource type and merge each one to the endpoint. Start with the closest to the endpoint.
  * 3) Get the traits as branches, as described in the spec, to get the order of the traits to apply.
  * 4) Resolve each trait and merge each one to the operation in the provided order..
  * 5) Remove 'extends' property from the endpoint and from the operations.
  */
class ExtendsResolutionStage(profile: String, val keepEditingInfo: Boolean, val fromOverlay: Boolean = false)
    extends ResolutionStage(profile)
    with PlatformSecrets {

  /** Default to raml10 context. */
  def ctx(parserRun: Int): RamlWebApiContext = profile match {
    case ProfileNames.RAML08 => new Raml08WebApiContext("", Nil, ParserContext(parserCount = parserRun))
    case _                   => new Raml10WebApiContext("", Nil, ParserContext(parserCount = parserRun))
  }

  override def resolve(model: BaseUnit): BaseUnit =
    model.transform(findExtendsPredicate, transform(model))

  def asEndPoint(r: ParametrizedResourceType, context: Context, apiContext: RamlWebApiContext): EndPoint = {
    Option(r.target) match {
      case Some(rt: ResourceType) =>
        val node = rt.dataNode.cloneNode()
        node.replaceVariables(context.variables)((message: String) =>
          apiContext.violation(r.id, message, r.annotations.find(classOf[LexicalInformation])))

        ExtendsHelper.asEndpoint(
          context.model,
          profile,
          node,
          r.name.value(),
          r.id,
          Option(r.target).flatMap(t => ExtendsHelper.findUnitLocationOfElement(t.id, context.model)),
          keepEditingInfo,
          Some(apiContext)
        )

      case _ =>
        apiContext.violation(r.id,
                             s"Cannot find target for parametrized resource type ${r.id}",
                             r.annotations.find(classOf[LexicalInformation]))
        ErrorEndPoint(r.id, r.annotations.find(classOf[SourceAST]).map(_.ast).getOrElse(YNode.Null))
    }
  }

  private def transform(model: BaseUnit)(element: DomainElement, isCycle: Boolean): Option[DomainElement] =
    element match {
      case e: EndPoint => Some(convert(model, e))
      case other       => Some(other)
    }

  private def collectResourceTypes(endpoint: EndPoint,
                                   context: Context,
                                   apiContext: RamlWebApiContext): ListBuffer[EndPoint] = {
    val result = ListBuffer[EndPoint]()
    collectResourceTypes(result, endpoint, context, apiContext)
    result
  }

  private def collectResourceTypes(collector: ListBuffer[EndPoint],
                                   endpoint: EndPoint,
                                   initial: Context,
                                   apiContext: RamlWebApiContext): Unit = {
    endpoint.resourceType.foreach { resourceType =>
      val context = initial.add(resourceType.variables)

      val resolved = asEndPoint(resourceType, context, apiContext)
      collector += resolved

      collectResourceTypes(collector, resolved, context, apiContext)
    }
  }

  /** Apply specified ResourceTypes to given EndPoint. */
  def apply(endpoint: EndPoint, resourceTypes: ListBuffer[EndPoint]): EndPoint = {
    resourceTypes.foldLeft(endpoint) {
      case (current, resourceType) => merge(current, resourceType)
    }
  }

  private def convert(model: BaseUnit, endpoint: EndPoint): EndPoint = {

    val context = Context(model)
      .add("resourcePath", resourcePath(endpoint))
      .add("resourcePathName", resourcePathName(endpoint))

    val resourceTypes = collectResourceTypes(endpoint, context, ctx(context.model.parserRun.get))
    apply(endpoint, resourceTypes) // Apply ResourceTypes to EndPoint

    val resolver = TraitResolver()

    // Iterate operations and resolve extends with inherited traits.
    endpoint.operations.foreach { operation =>
      val local = context.add("methodName", operation.method.value())

      val branches = ListBuffer[BranchContainer]()

      // Method branch
      branches += Branches.method(resolver, operation, local)

      // EndPoint branch
      branches += Branches.endpoint(resolver, endpoint, local)

      // ResourceType branches
      resourceTypes.foreach { rt =>
        branches += Branches.resourceType(resolver, rt, local, operation.method.value())
      }

      // Compute final traits
      val traits = branches.foldLeft(Seq[TraitBranch]()) {
        case (current, container) =>
          BranchContainer.merge(current, container.flatten()).collect { case t: TraitBranch => t }
      }

      // Merge traits into operation
      traits.foldLeft(operation) {
        case (current, branch) => DomainElementMerging.merge(current, branch.operation)
      }

      // This is required in the case where the extension comes from an overlay/extension
      if (!keepEditingInfo && !fromOverlay) operation.fields.removeField(DomainElementModel.Extends)
    }

    // This is required in the case where the extension comes from an overlay/extension
    if (!keepEditingInfo && !fromOverlay) endpoint.fields.removeField(DomainElementModel.Extends)

    endpoint
  }

  private def resourcePathName(endPoint: EndPoint): String = {
    resourcePath(endPoint)
      .split('/')
      .reverse
      .find(s => s.nonEmpty && "\\{.*\\}".r.findFirstIn(s).isEmpty)
      .getOrElse("")
  }

  private def resourcePath(endPoint: EndPoint) = endPoint.path.value().replaceAll("\\{ext\\}", "")

  private def findExtendsPredicate(element: DomainElement): Boolean = element.isInstanceOf[EndPoint]

  object Branches {
    def apply(branches: Seq[Branch]): BranchContainer = BranchContainer(branches)

    def endpoint(resolver: TraitResolver, endpoint: EndPoint, context: Context): BranchContainer = {
      BranchContainer(resolveTraits(resolver, endpoint.traits, context))
    }

    def resourceType(traits: TraitResolver,
                     resourceType: EndPoint,
                     context: Context,
                     operation: String): BranchContainer = {

      // Resolve resource type method traits
      val o = resourceType.operations
        .find(_.method.value() == operation)
        .map(method(traits, _, context).flatten())
        .getOrElse(Seq())

      // Resolve resource type traits
      val e = endpoint(traits, resourceType, context).flatten()

      BranchContainer(BranchContainer.merge(o, e))
    }

    def method(resolver: TraitResolver, operation: Operation, context: Context): BranchContainer = {
      BranchContainer(resolveTraits(resolver, operation.traits, context))
    }

    private def resolveTraits(resolver: TraitResolver, parameterized: Seq[ParametrizedTrait], context: Context) = {
      parameterized.map(resolver.resolve(_, context, ctx(context.model.parserRun.get)))
    }
  }

  case class ResourceTypeResolver(model: BaseUnit) {
    val resolved: mutable.Map[Key, TraitBranch] = mutable.Map()
  }

  case class TraitResolver() {

    val resolved: mutable.Map[Key, TraitBranch] = mutable.Map()

    def resolve(t: ParametrizedTrait, context: Context, apiContext: RamlWebApiContext): TraitBranch = {
      val local = context.add(t.variables)
      val key   = Key(t.target.id, local)
      resolved.getOrElseUpdate(key, resolveOperation(key, t, context, apiContext))
    }

    private def resolveOperation(key: Key,
                                 parameterized: ParametrizedTrait,
                                 context: Context,
                                 apiContext: RamlWebApiContext): TraitBranch = {
      val local = context.add(parameterized.variables)

      Option(parameterized.target) match {
        case Some(t: ErrorDeclaration) => TraitBranch(key, Operation(), Seq())
        case Some(t: Trait) =>
          val node: DataNode = t.dataNode.cloneNode()
          node.replaceVariables(local.variables)((message: String) =>
            apiContext.violation(t.id, message, t.annotations.find(classOf[LexicalInformation])))

          val op = ExtendsHelper.asOperation(
            profile,
            node,
            context.model,
            t.name.option().getOrElse(""),
            t.id,
            ExtendsHelper.findUnitLocationOfElement(t.id, context.model),
            keepEditingInfo,
            Some(apiContext)
          )

          val children = op.traits.map(resolve(_, context, apiContext))

          TraitBranch(key, op, children)
        case m => throw new Exception(s"Looking for trait but $m was found on model ${context.model}")
      }
    }
  }

  case class TraitBranch(key: Key, operation: Operation, children: Seq[Branch]) extends Branch

}
