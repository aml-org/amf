package amf.client.resolve
import amf.client.convert.{BidirectionalMatcher, ClientInternalMatcher}
import amf.core.annotations.LexicalInformation
import amf.core.parser.ErrorHandler
import amf.client.convert.CoreClientConverters._

object ClientErrorHandlerConverter {

  implicit object RangeToLexicalConverter extends BidirectionalMatcher[LexicalInformation, amf.core.parser.Range] {

    override def asInternal(from: amf.core.parser.Range): LexicalInformation = LexicalInformation(from)

    override def asClient(from: LexicalInformation): amf.core.parser.Range = from.range
  }

  implicit object ErrorHandlerConverter extends ClientInternalMatcher[ClientErrorHandler, ErrorHandler] {

    override def asInternal(from: ClientErrorHandler): ErrorHandler = convert(from)
  }

  def convert(clientErrorHandler: ClientErrorHandler): ErrorHandler =
    new ErrorHandler {
      override protected def reportConstraint(id: String,
                                              node: String,
                                              property: Option[String],
                                              message: String,
                                              range: Option[LexicalInformation],
                                              level: String,
                                              location: Option[String]): Unit = {
        clientErrorHandler.reportConstraint(id,
                                            node,
                                            property.asClient,
                                            message,
                                            range.asClient,
                                            level,
                                            location.asClient)
      }
    }
}
