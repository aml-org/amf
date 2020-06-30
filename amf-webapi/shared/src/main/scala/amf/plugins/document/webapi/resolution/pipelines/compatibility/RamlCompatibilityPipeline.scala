package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.{AmfElement, Shape}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.stages.ResolutionStage
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.ParsedFromJsonSchema
import amf.plugins.document.webapi.resolution.pipelines.compatibility.raml._
import amf.plugins.domain.webapi.models.SchemaContainer
import amf.plugins.domain.webapi.resolution.stages.RamlCompatiblePayloadAndParameterResolutionStage
import amf.{ProfileName, RamlProfile}

class RamlCompatibilityPipeline(override val eh: ErrorHandler) extends ResolutionPipeline(eh) {

  override val steps: Seq[ResolutionStage] = Seq(
    new MandatoryDocumentationTitle(),
    new MandatoryAnnotationType(),
    new DefaultPayloadMediaType(),
    new MandatoryCreativeWorkFields(),
    new DefaultToNumericDefaultResponse(),
    new MakeExamplesOptional(),
    new CapitalizeSchemes(),
    new SecuritySettingsMapper(),
    new ShapeFormatAdjuster(),
    new CustomAnnotationDeclaration(),
    new PushSingleOperationPathParams(),
    new UnionsAsTypeExpressions(),
    new EscapeTypeNames(),
    new MakeRequiredFieldImplicitForOptionalProperties(),
    new ResolveRamlCompatibleDeclarations(),
    new ExtractCommonShapesToDeclarations(),
    new ResolveLinksWithNonDeclaredTargets(),
    new RamlCompatiblePayloadAndParameterResolutionStage(profileName),
    new SanitizeCustomTypeNames(),
    new RecursionDetection()
  )

  override def profileName: ProfileName = RamlProfile
}

object RamlCompatibilityPipeline {
  def unhandled = new RamlCompatibilityPipeline(UnhandledErrorHandler)
}

class ExtractCommonShapesToDeclarations()(implicit errorHandler: ErrorHandler)
    extends ResolutionStage()(errorHandler) {

  override def resolve[T <: BaseUnit](model: T): T = {
    model match {
      case doc: Document =>
        resolve(doc)
        model
      case _ => model
    }
  }

  private def resolve(doc: Document): Unit = {
    val schemaContainersWithJsonSchemaShapes = getSchemaContainers(doc)
    val externalEmbeddedSchemas              = groupSchemas(schemaContainersWithJsonSchemaShapes)
    val idCounter                            = new IdCounter()
    externalEmbeddedSchemas.foreach {
      case (_, containers) =>
        val schema = containers.head.schema
        doc.withDeclaredElement(schema)
        val label = idCounter.genId("Generated")
        schema.withName(label)
        val link = schema.link[Shape](label)
        containers.foreach(_.setSchema(link))
    }
  }

  private def groupSchemas(schemaContainersWithJsonSchemaShapes: List[AmfElement with SchemaContainer]) = {
    schemaContainersWithJsonSchemaShapes
      .groupBy(x => x.schema.effectiveLinkTarget().annotations.find(classOf[ParsedFromJsonSchema]).get.fullRef)
      .filter {
        case (_, containers) =>
          containers.length > 1 && containers.head.schema
            .effectiveLinkTarget()
            .annotations
            .find(classOf[ParsedFromJsonSchema])
            .get
            .fragment
            .nonEmpty
      }
  }

  private def getSchemaContainers(doc: Document) = {
    doc
      .iterator()
      .collect {
        case container: SchemaContainer if hasJsonSchemaShape(container) => container
      }
      .toList
  }

  private def hasJsonSchemaShape(container: SchemaContainer): Boolean =
    Option(container.schema) match {
      case Some(schema) => schema.effectiveLinkTarget().annotations.contains(classOf[ParsedFromJsonSchema])
      case None         => false
    }
}
