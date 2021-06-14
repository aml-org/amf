package amf.plugins.document.apicontract.resolution.stages

import amf.client.errorhandling.IgnoringErrorHandler
import amf.client.remod.ParseConfiguration
import amf.core.annotations.SourceAST
import amf.core.client.common.validation.{ProfileName, Raml08Profile}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{DataNode, DomainElement, ElementTree}
import amf.core.client.scala.parse.document.ParserContext
import amf.core.client.scala.transform.stages.{ReferenceResolutionStage, TransformationStep}
import amf.core.emitter.SpecOrdering
import amf.core.errorhandling.AMFErrorHandler
import amf.core.internal.annotations.{ErrorDeclaration, SourceAST}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.ParseConfiguration
import amf.core.internal.render.SpecOrdering
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils.AliasCounter
import amf.core.internal.validation.CoreValidations.ResolutionValidation
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{DataNode, DomainElement, ElementTree}
import amf.core.parser.{ParserContext, YNodeLikeOps}
import amf.core.resolution.stages.{ReferenceResolutionStage, TransformationStep}
import amf.core.unsafe.PlatformSecrets
import amf.core.internal.utils.AliasCounter
import amf.plugins.document.apicontract.contexts.emitter.raml.Raml10SpecEmitterContext
import amf.plugins.document.apicontract.contexts.parser.raml.{
  Raml08WebApiContext,
  Raml10WebApiContext,
  RamlWebApiContext
}
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations.{ErrorEndPoint, ErrorTrait}
import amf.plugins.document.apicontract.parser.spec.domain.{Raml10EndPointEmitter, Raml10OperationEmitter}
import amf.plugins.domain.apicontract.models.templates.{
  ParametrizedResourceType,
  ParametrizedTrait,
  ResourceType,
  Trait
}
import amf.plugins.domain.apicontract.models.{EndPoint, Operation}
import amf.plugins.domain.apicontract.resolution.ExtendsHelper
import amf.plugins.domain.apicontract.resolution.stages.DomainElementMerging
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import amf.validations.ShapeParserSideValidations.ExceededMaxYamlReferences
import amf.{ProfileName, Raml08Profile}
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * 1) Get a queue of resource types for this Endpoint.
  * 2) Resolve each resource type and merge each one to the endpoint. Start with the closest to the endpoint.
  * 3) Get the traits as branches, as described in the spec, to get the order of the traits to apply.
  * 4) Resolve each trait and merge each one to the operation in the provided order..
  * 5) Remove 'extends' property from the endpoint and from the operations.
  */
class ExtendsResolutionStage(profile: ProfileName, val keepEditingInfo: Boolean, val fromOverlay: Boolean = false)
    extends TransformationStep()
    with PlatformSecrets {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit =
    new ExtendsResolution(profile, keepEditingInfo, fromOverlay)(errorHandler).resolve(model)

  class ExtendsResolution(profile: ProfileName,
                          val keepEditingInfo: Boolean,
                          val fromOverlay: Boolean = false,
                          visited: mutable.Set[String] = mutable.Set())(implicit val errorHandler: AMFErrorHandler) {

    /** Default to raml10 context. */
    def ctx(): RamlWebApiContext = profile match {
      case Raml08Profile =>
        new Raml08WebApiContext("", Nil, ParserContext(config = ParseConfiguration(errorHandler)))
      case _ => new Raml10WebApiContext("", Nil, ParserContext(config = ParseConfiguration(errorHandler)))
    }

    def resolve[T <: BaseUnit](model: T): T =
      model.transform(findExtendsPredicate, transform(model)).asInstanceOf[T]

    def asEndPoint(r: ParametrizedResourceType,
                   context: Context,
                   apiContext: RamlWebApiContext,
                   tree: ElementTree): EndPoint = {
      Option(r.target) match {
        case Some(rt: ResourceType) =>
          val node = rt.dataNode.copyNode()
          node.replaceVariables(context.variables, tree.subtrees)((message: String) =>
            apiContext.eh.violation(ResolutionValidation, r.id, None, message, r.position(), r.location()))
          val extendsHelper = ExtendsHelper(profile, keepEditingInfo = keepEditingInfo, errorHandler, Some(apiContext))
          extendsHelper.asEndpoint(
            context.model,
            node,
            rt.annotations,
            r.name.value(),
            r.id
          )

        case _ =>
          apiContext.eh.violation(ResolutionValidation,
                                  r.id,
                                  None,
                                  s"Cannot find target for parametrized resource type ${r.id}",
                                  r.position(),
                                  r.location())
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
                                     apiContext: RamlWebApiContext,
                                     tree: ElementTree): ListBuffer[EndPoint] = {
      val result = ListBuffer[EndPoint]()

      collectResourceTypes(result, endpoint, context, apiContext, tree)
      result
    }

    private def collectResourceTypes(collector: ListBuffer[EndPoint],
                                     endpoint: EndPoint,
                                     initial: Context,
                                     apiContext: RamlWebApiContext,
                                     tree: ElementTree): Unit = {
      endpoint.resourceType.foreach { resourceType =>
        val context = initial.add(resourceType.variables)

        val resolved = asEndPoint(resourceType, context, apiContext, tree)
        collector += resolved

        collectResourceTypes(collector, resolved, context, apiContext, tree)
      }
    }

    /** Apply specified ResourceTypes to given EndPoint. */
    def apply(endpoint: EndPoint, resourceTypes: ListBuffer[EndPoint])(implicit ctx: RamlWebApiContext): EndPoint = {
      resourceTypes.foldLeft(endpoint) {
        case (current, resourceType) => DomainElementMerging()(ctx).merge(current, resourceType)
      }
    }

    private def convert(model: BaseUnit, endpoint: EndPoint): EndPoint = {

      val context = Context(model)
        .add("resourcePath", resourcePath(endpoint))
        .add("resourcePathName", resourcePathName(endpoint))

      val tree           = EndPointTreeBuilder(endpoint).build()
      val extendsContext = ctx()
      val resourceTypes  = collectResourceTypes(endpoint, context, extendsContext, tree)
      apply(endpoint, resourceTypes)(extendsContext) // Apply ResourceTypes to EndPoint

      val resolver = TraitResolver()

      // Iterate operations and resolve extends with inherited traits.
      val traitList = endpoint.operations.flatMap { operation =>
        val local = context.add("methodName", operation.method.value())

        val branches = ListBuffer[BranchContainer]()

        val operationTree = OperationTreeBuilder(operation)(IgnoringErrorHandler).build()
        val branchesObj   = Branches()(extendsContext)

        // Method branch
        branches += branchesObj.method(resolver, operation, local, operationTree)

        // EndPoint branch
        branches += branchesObj.endpoint(resolver, endpoint, local, tree)

        // ResourceType branches
        resourceTypes.foreach { rt =>
          branches += branchesObj.resourceType(resolver, rt, local, operation.method.value(), tree)
        }

        // Compute final traits
        val traits = branches.foldLeft(Seq[TraitBranch]()) {
          case (current, container) =>
            BranchContainer.merge(current, container.flatten()).collect { case t: TraitBranch => t }
        }

        // Merge traits into operation
        traits.foldLeft(operation) {
          case (current, branch) => DomainElementMerging()(extendsContext).merge(current, branch.operation)
        }

        // This is required in the case where the extension comes from an overlay/extension
        if (!keepEditingInfo && !fromOverlay) operation.fields.removeField(DomainElementModel.Extends)

        traits
      }

      // This is required in the case where the extension comes from an overlay/extension
      if (!keepEditingInfo && !fromOverlay) endpoint.fields.removeField(DomainElementModel.Extends)

      extendsContext.futureDeclarations.resolve()
      if (resourceTypes.nonEmpty || traitList.nonEmpty)
        new ReferenceResolutionStage(keepEditingInfo)
          .resolveDomainElement(endpoint, errorHandler) // TODO revise why this is not working
      else
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

    private def findExtendsPredicate(element: DomainElement): Boolean = {
      if (visited.contains(element.id) && !fromOverlay) true
      else {
        visited += element.id
        element.isInstanceOf[EndPoint]
      }
    }

    case class Branches()(implicit extendsContext: RamlWebApiContext) {
      def apply(branches: Seq[Branch]): BranchContainer = BranchContainer(branches)

      def endpoint(resolver: TraitResolver, endpoint: EndPoint, context: Context, tree: ElementTree): BranchContainer = {
        BranchContainer(resolveTraits(resolver, endpoint.traits, context, tree.subtrees))
      }

      def resourceType(traits: TraitResolver,
                       resourceType: EndPoint,
                       context: Context,
                       operation: String,
                       tree: ElementTree): BranchContainer = {

        // Resolve resource type method traits
        val o = resourceType.operations
          .find(_.method.value() == operation)
          .map(op => {
            method(traits,
                   op,
                   context,
                   tree.subtrees.find(_.key.equals(operation)).getOrElse(ElementTree(operation, Nil))).flatten()
          })
          .getOrElse(Seq())

        // Resolve resource type traits
        val e = endpoint(traits, resourceType, context, tree).flatten()

        BranchContainer(BranchContainer.merge(o, e))
      }

      def method(resolver: TraitResolver, operation: Operation, context: Context, tree: ElementTree): BranchContainer = {
        BranchContainer(resolveTraits(resolver, operation.traits, context, tree.subtrees))
      }

      private def resolveTraits(resolver: TraitResolver,
                                parameterized: Seq[ParametrizedTrait],
                                context: Context,
                                subTree: Seq[ElementTree]) = {
        parameterized.flatMap(resolver.resolve(_, context, extendsContext, subTree))
      }
    }

    case class ResourceTypeResolver(model: BaseUnit) {
      val resolved: mutable.Map[Key, TraitBranch] = mutable.Map()
    }

    case class TraitResolver() {

      val resolved: mutable.Map[Key, TraitBranch] = mutable.Map()

      def resolve(t: ParametrizedTrait,
                  context: Context,
                  apiContext: RamlWebApiContext,
                  subTree: Seq[ElementTree]): Option[TraitBranch] = {
        val local = context.add(t.variables)
        val key   = Key(t.target.id, local)
        resolveOperation(key, t, context, apiContext, subTree) match {
          case Some(ro) => Some(resolved.getOrElseUpdate(key, ro))
          case _ =>
            resolved -= key
            None
        }
      }

      private def resolveOperation(key: Key,
                                   parameterized: ParametrizedTrait,
                                   context: Context,
                                   apiContext: RamlWebApiContext,
                                   subTree: Seq[ElementTree]): Option[TraitBranch] = {
        val local = context.add(parameterized.variables)

        Option(parameterized.target) match {
          case Some(_: ErrorDeclaration[_]) => Some(TraitBranch(key, Operation(), Seq()))
          case Some(potentialTrait: Trait) =>
            potentialTrait.effectiveLinkTarget() match {
              case err: ErrorTrait =>
                Some(TraitBranch(key, Operation().withId(err.id + "_op"), Nil))
              case t: Trait =>
                val node: DataNode = t.dataNode.copyNode()
                node.replaceVariables(local.variables, subTree)((message: String) => {
                  apiContext.eh.violation(ResolutionValidation,
                                          t.id,
                                          None,
                                          message,
                                          parameterized.position(),
                                          parameterized.location())
                })

                val extendsHelper = ExtendsHelper(profile, keepEditingInfo = true, errorHandler, Some(apiContext))
                val op = extendsHelper.asOperation(
                  node,
                  context.model,
                  parameterized.name.option().getOrElse(""),
                  parameterized.annotations,
                  t.id
                )

                val children = op.traits.flatMap(resolve(_, context, apiContext, subTree))

                Some(TraitBranch(key, op, children))
            }
          case m =>
            errorHandler.violation(
              ResolutionValidation,
              parameterized.id,
              s"Looking for trait but $m was found on model ${context.model}",
              parameterized.annotations
            )
            None
        }
      }
    }

    case class TraitBranch(key: Key, operation: Operation, children: Seq[Branch]) extends Branch

    private abstract class ElementTreeBuilder(element: DomainElement) {
      private val refsCounter = AliasCounter()

      private def buildEntry(entry: YMapEntry) = {
        val sons: Seq[ElementTree] = buildNode(entry.value)
        ElementTree(entry.key.toString(), sons)
      }

      private def buildNode(node: YNode): Seq[ElementTree] = {
        withRefCounterGuard(node) {
          node.value match {
            case map: YMap      => map.entries.map { buildEntry }
            case seq: YSequence => seq.nodes.flatMap { buildNode }
            case _              => Nil
          }
        }
      }

      private def withRefCounterGuard(node: YNode)(thunk: => Seq[ElementTree]) = {
        if (refsCounter.exceedsThreshold(node)) {
          errorHandler.violation(
            ExceededMaxYamlReferences,
            "",
            "Exceeded maximum yaml references threshold"
          )
          Nil
        } else thunk
      }

      def build(): ElementTree = {
        val node: YNode =
          element.annotations.find(classOf[SourceAST]).map(_.ast).collectFirst({ case e: YMapEntry => e }) match {
            case Some(entry) => entry.value
            case _           => astFromEmition
          }
        ElementTree(rootKey, buildNode(node))
      }

      protected def astFromEmition: YNode
      protected val rootKey: String
    }

    private case class EndPointTreeBuilder(endpoint: EndPoint) extends ElementTreeBuilder(endpoint) {
      override protected def astFromEmition: YNode =
        YDocument(f =>
          f.obj(
            Raml10EndPointEmitter(endpoint, SpecOrdering.Lexical)(new Raml10SpecEmitterContext(errorHandler)).emit)).node
          .toOption[YMap]
          .map(_.entries)
          .getOrElse(Nil)
          .headOption
          .map(_.value)
          .getOrElse(YNode.Null)

      override protected val rootKey: String = endpoint.path.value()
    }

    private case class OperationTreeBuilder(operation: Operation)(implicit errorHandler: IllegalTypeHandler)
        extends ElementTreeBuilder(operation) {
      override protected def astFromEmition: YNode =
        YDocument(f => emitOperation(operation, f)).node
          .as[YMap]
          .entries
          .headOption
          .map(_.value)
          .getOrElse(YNode.Null)

      override protected val rootKey: String = operation.method.value()
    }

    private def emitOperation(operation: Operation, f: PartBuilder): Unit = {
      f.obj(
        Raml10OperationEmitter(operation, SpecOrdering.Lexical, Nil)(new Raml10SpecEmitterContext(errorHandler)).emit)
    }
  }

}
