package amf.apicontract.internal.spec.common

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.parser.domain.{FutureDeclarations, JsonPointerQualifiedNameExtractor, QualifiedNameExtractor}
import org.yaml.model.YNode

class OasLikeWebApiDeclarations(
    val asts: Map[String, YNode],
    override val alias: Option[String],
    override val errorHandler: AMFErrorHandler,
    override val futureDeclarations: FutureDeclarations,
    override val extractor: QualifiedNameExtractor = JsonPointerQualifiedNameExtractor
) extends WebApiDeclarations(alias, errorHandler, futureDeclarations, extractor) {}
