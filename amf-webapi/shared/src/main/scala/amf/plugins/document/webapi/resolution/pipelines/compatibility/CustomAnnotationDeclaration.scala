package amf.plugins.document.webapi.resolution.pipelines.compatibility

import amf.core.metamodel.{Field, Obj}
import amf.core.model.document.{BaseUnit, DeclaresModel}
import amf.core.model.domain.DomainElement
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.{Annotations, ErrorHandler, Fields}
import amf.core.resolution.stages.ResolutionStage
import amf.core.utils.Strings
import amf.plugins.domain.shapes.models.AnyShape

class CustomAnnotationDeclaration()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {
  override def resolve[T <: BaseUnit](model: T): T = {
    try {
      val annotationsTypes: Seq[DomainElement] = Seq("url", "format", "exclusiveMaximum", "exclusiveMinimum", "minimum",
        "maximum", "format", "tuple", "collectionFormat", "additionalProperties", "dependencies", "readOnly",
        "externalDocs", "format", "recursive", "pattern", "multipleOf", "collectionFormat", "payloads",
        "baseUriParameters", "oasDeprecated", "summary", "tags", "consumes", "produces", "binding", "flow",
        "parameters", "defaultResponse", "examples", "serverDescription", "servers", "responses", "termsOfService",
        "contact", "license", "and", "or", "xor", "not", "xone")
        .map(name => CustomDomainProperty().withName(s"amf-$name").withSchema(AnyShape()))

      model match {
        case d: DeclaresModel =>
          annotationsTypes.foreach(d.withDeclaredElement)
          model
        case _ =>
          model
      }
    } catch {
      case _: Throwable => model
    }
  }
}
