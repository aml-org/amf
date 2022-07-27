package amf.apicontract.internal.transformation.stages

import amf.apicontract.client.scala.model.domain.templates.{
  ParametrizedResourceType,
  ParametrizedTrait,
  ResourceType,
  Trait
}
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.apicontract.internal.spec.common.WebApiDeclarations.{ErrorEndPoint, ErrorTrait}
import amf.apicontract.internal.spec.common.emitter.{Raml10EndPointEmitter, Raml10OperationEmitter}
import amf.apicontract.internal.spec.common.transformation.ExtendsHelper
import amf.apicontract.internal.spec.common.transformation.stage.DomainElementMerging
import amf.apicontract.internal.spec.raml.emitter.context.Raml10SpecEmitterContext
import amf.apicontract.internal.spec.raml.parser.context.{Raml08WebApiContext, Raml10WebApiContext, RamlWebApiContext}
import amf.core.client.common.validation.{ProfileName, Raml08Profile}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{DataNode, DomainElement, ElementTree}
import amf.core.client.scala.parse.document.ParserContext
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.annotations.{ErrorDeclaration, SourceAST, SourceYPart}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.{ParseConfig, YNodeLikeOps}
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.plugins.syntax.SYamlAMFParserErrorHandler
import amf.core.internal.render.SpecOrdering
import amf.core.internal.transform.stages.ReferenceResolutionStage
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils.AliasCounter
import amf.core.internal.validation.CoreValidations.{ExceededMaxYamlReferences, TransformationValidation}
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**   1. Get a queue of resource types for this Endpoint.
  *   1. Resolve each resource type and merge each one to the endpoint. Start with the closest to the endpoint.
  *   1. Get the traits as branches, as described in the spec, to get the order of the traits to apply.
  *   1. Resolve each trait and merge each one to the operation in the provided order..
  *   1. Remove 'extends' property from the endpoint and from the operations.
  */
class ExtendsResolutionStage(profile: ProfileName, val keepEditingInfo: Boolean, val fromOverlay: Boolean = false)
    extends TransformationStep()
    with PlatformSecrets {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit =
    new ExtendsResolution(profile, keepEditingInfo, fromOverlay, config = configuration)(errorHandler)
      .transform(model, configuration)

  class ExtendsResolution(
      profile: ProfileName,
      val keepEditingInfo: Boolean,
      val fromOverlay: Boolean = false,
      visited: mutable.Set[String] = mutable.Set(),
      config: AMFGraphConfiguration
  )(implicit val errorHandler: AMFErrorHandler) {

    /** Default to raml10 context. */
    def ctx(): RamlWebApiContext = profile match {
      case Raml08Profile =>
        new Raml08WebApiContext("", Nil, ParserContext(config = ParseConfig(config, errorHandler)))
      case _ =>
        new Raml10WebApiContext("", Nil, ParserContext(config = ParseConfig(config, errorHandler)))
    }

    def transform[T <: BaseUnit](model: T, configuration: AMFGraphConfiguration): T =
      model.transform(findExtendsPredicate, transform(model)(_, _, configuration)).asInstanceOf[T]

    def asEndPoint(
        r: ParametrizedResourceType,
        context: Context,
        apiContext: RamlWebApiContext,
        tree: ElementTree,
        configuration: AMFGraphConfiguration
    ): EndPoint = {
      Option(r.target) match {
        case Some(rt: ResourceType) =>
          val node = rt.dataNode.copyNode()
          node.replaceVariables(context.variables, tree.subtrees)((message: String) =>
            apiContext.eh.violation(TransformationValidation, r.id, None, message, r.position(), r.location())
          )
          val extendsHelper =
            ExtendsHelper(profile, keepEditingInfo = keepEditingInfo, errorHandler, configuration, Some(apiContext))
          val result = extendsHelper.asEndpoint(
            context.model,
            node,
            rt.annotations,
            r.name.value(),
            r.id,
            configuration
          )
          result

        case _ =>
          apiContext.eh.violation(TransformationValidation,
                                  r.id,
                                  None,
                                  s"Cannot find target for parametrized resource type ${r.id}",
                                  r.position(),
                                  r.location())
          ErrorEndPoint(r.id, r.annotations.find(classOf[SourceYPart]).map(_.ast).getOrElse(YNode.Null))
      }
    }

    private def transform(
        model: BaseUnit
    )(element: DomainElement, isCycle: Boolean, configuration: AMFGraphConfiguration): Option[DomainElement] =
      element match {
        case e: EndPoint => Some(convert(model, e, configuration))
        case other       => Some(other)
      }

    private def collectResourceTypes(
        endpoint: EndPoint,
        context: Context,
        apiContext: RamlWebApiContext,
        tree: ElementTree,
        configuration: AMFGraphConfiguration
    ): ListBuffer[EndPoint] = {
      val result = ListBuffer[EndPoint]()

      collectResourceTypes(result, endpoint, context, apiContext, tree, configuration)
      result
    }

    private def collectResourceTypes(
        collector: ListBuffer[EndPoint],
        endpoint: EndPoint,
        initial: Context,
        apiContext: RamlWebApiContext,
        tree: ElementTree,
        configuration: AMFGraphConfiguration
    ): Unit = {
      endpoint.resourceType.foreach { resourceType =>
        val context = initial.add(resourceType.variables)

        val resolved = asEndPoint(resourceType, context, apiContext, tree, configuration)
        collector += resolved

        collectResourceTypes(collector, resolved, context, apiContext, tree, configuration)
      }
    }

    /** Apply specified ResourceTypes to given EndPoint. */
    def apply(endpoint: EndPoint, resourceTypes: ListBuffer[EndPoint])(implicit ctx: RamlWebApiContext): EndPoint = {
      resourceTypes.foldLeft(endpoint) { case (current, resourceType) =>
        DomainElementMerging()(ctx).merge(current, resourceType)
      }
    }

    private def convert(model: BaseUnit, endpoint: EndPoint, configuration: AMFGraphConfiguration): EndPoint = {

      val context = Context(model)
        .add("resourcePath", resourcePath(endpoint))
        .add("resourcePathName", resourcePathName(endpoint))

      val tree           = EndPointTreeBuilder(endpoint).build()
      val extendsContext = ctx()
      val resourceTypes  = collectResourceTypes(endpoint, context, extendsContext, tree, configuration)
      apply(endpoint, resourceTypes)(extendsContext) // Apply ResourceTypes to EndPoint

      val resolver = TraitTransformer()

      // Iterate operations and resolve extends with inherited traits.
      val traitList = endpoint.operations.toList.flatMap { operation =>
        val local = context.add("methodName", operation.method.value())

        val branches = ListBuffer[BranchContainer]()

        val operationTree =
          OperationTreeBuilder(operation)(new SYamlAMFParserErrorHandler(IgnoringErrorHandler)).build()
        val branchesObj = Branches()(extendsContext)

        // Method branch
        branches += branchesObj.method(resolver, operation, local, operationTree, configuration)

        // EndPoint branch
        branches += branchesObj.endpoint(resolver, endpoint, local, tree, configuration)

        // ResourceType branches
        resourceTypes.foreach { rt =>
          branches += branchesObj.resourceType(resolver, rt, local, operation.method.value(), tree, configuration)
        }

        // Compute final traits
        val traits = branches.foldLeft(Seq[TraitBranch]()) { case (current, container) =>
          BranchContainer.merge(current, container.flatten()).collect { case t: TraitBranch => t }
        }

        // Merge traits into operation
        traits.foldLeft(operation) { case (current, branch) =>
          DomainElementMerging()(extendsContext).merge(current, branch.operation)
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
          .resolveDomainElement(endpoint, errorHandler, configuration)
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

    private def resourcePath(endPoint: EndPoint) =
      endPoint.path.option().map(_.replaceAll("\\{ext\\}", "")).getOrElse("")

    private def findExtendsPredicate(element: DomainElement): Boolean = {
      if (visited.contains(element.id) && !fromOverlay) true
      else {
        visited += element.id
        element.isInstanceOf[EndPoint]
      }
    }

    case class Branches()(implicit extendsContext: RamlWebApiContext) {
      def apply(branches: Seq[Branch]): BranchContainer = BranchContainer(branches)

      def endpoint(
          resolver: TraitTransformer,
          endpoint: EndPoint,
          context: Context,
          tree: ElementTree,
          configuration: AMFGraphConfiguration
      ): BranchContainer = {
        BranchContainer(transformTraits(resolver, endpoint.traits, context, tree.subtrees, configuration))
      }

      def resourceType(
          traits: TraitTransformer,
          resourceType: EndPoint,
          context: Context,
          operation: String,
          tree: ElementTree,
          configuration: AMFGraphConfiguration
      ): BranchContainer = {

        // Resolve resource type method traits
        val o = resourceType.operations
          .find(_.method.value() == operation)
          .map(op => {
            method(
              traits,
              op,
              context,
              tree.subtrees.find(_.key.equals(operation)).getOrElse(ElementTree(operation, Nil)),
              configuration
            ).flatten()
          })
          .getOrElse(Seq())

        // Resolve resource type traits
        val e = endpoint(traits, resourceType, context, tree, configuration).flatten()

        BranchContainer(BranchContainer.merge(o, e))
      }

      def method(
          transformer: TraitTransformer,
          operation: Operation,
          context: Context,
          tree: ElementTree,
          configuration: AMFGraphConfiguration
      ): BranchContainer = {
        BranchContainer(transformTraits(transformer, operation.traits, context, tree.subtrees, configuration))
      }

      private def transformTraits(
          resolver: TraitTransformer,
          parameterized: Seq[ParametrizedTrait],
          context: Context,
          subTree: Seq[ElementTree],
          configuration: AMFGraphConfiguration
      ) = {
        parameterized.flatMap(resolver.transform(_, context, extendsContext, subTree, configuration))
      }
    }

    case class ResourceTypeTransformer(model: BaseUnit) {
      val transformed: mutable.Map[Key, TraitBranch] = mutable.Map()
    }

    case class TraitTransformer() {

      val transformed: mutable.Map[Key, TraitBranch] = mutable.Map()

      def transform(
          t: ParametrizedTrait,
          context: Context,
          apiContext: RamlWebApiContext,
          subTree: Seq[ElementTree],
          configuration: AMFGraphConfiguration
      ): Option[TraitBranch] = {
        val local = context.add(t.variables)
        val key   = Key(t.target.id, local)
        transformOperation(key, t, context, apiContext, subTree, configuration) match {
          case Some(ro) => Some(transformed.getOrElseUpdate(key, ro))
          case _ =>
            transformed -= key
            None
        }
      }

      private def transformOperation(
          key: Key,
          parameterized: ParametrizedTrait,
          context: Context,
          apiContext: RamlWebApiContext,
          subTree: Seq[ElementTree],
          configuration: AMFGraphConfiguration
      ): Option[TraitBranch] = {
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
                  apiContext.eh.violation(
                    TransformationValidation,
                    t.id,
                    None,
                    message,
                    parameterized.position(),
                    parameterized.location()
                  )
                })

                val extendsHelper =
                  ExtendsHelper(profile, keepEditingInfo = true, errorHandler, configuration, Some(apiContext))
                val op = extendsHelper.asOperation(
                  node,
                  context.model,
                  parameterized.name.option().getOrElse(""),
                  parameterized.annotations,
                  t.id,
                  configuration
                )

                val children = op.traits.flatMap(transform(_, context, apiContext, subTree, configuration))

                Some(TraitBranch(key, op, children))
            }
          case m =>
            errorHandler.violation(
              TransformationValidation,
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
          element.annotations.find(classOf[SourceYPart]).map(_.ast).collectFirst({ case e: YMapEntry => e }) match {
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
            Raml10EndPointEmitter(endpoint, SpecOrdering.Lexical)(
              new Raml10SpecEmitterContext(errorHandler, config = RenderConfiguration.empty(errorHandler))
            ).emit
          )
        ).node
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
        Raml10OperationEmitter(operation, SpecOrdering.Lexical, Nil)(
          new Raml10SpecEmitterContext(errorHandler, config = RenderConfiguration.empty(errorHandler))
        ).emit
      )
    }
  }

}
