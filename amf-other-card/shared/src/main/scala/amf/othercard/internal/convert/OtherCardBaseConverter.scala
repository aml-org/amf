package amf.othercard.internal.convert

import amf.core.internal.convert.BidirectionalMatcher
import amf.othercard.client.platform.{OtherCardConfiguration => ClientOtherCardConfiguration}
import amf.othercard.client.scala.OtherCardConfiguration
import amf.shapes.internal.convert.ShapesBaseConverter

trait OtherCardBaseConverter
    extends ShapesBaseConverter
    with OtherCardConfigurationConverter

trait OtherCardConfigurationConverter {
  implicit object OtherCardConfigurationMatcher
      extends BidirectionalMatcher[OtherCardConfiguration, ClientOtherCardConfiguration] {
    override def asClient(from: OtherCardConfiguration): ClientOtherCardConfiguration = new ClientOtherCardConfiguration(from)

    override def asInternal(from: ClientOtherCardConfiguration): OtherCardConfiguration = from._internal
  }
}
