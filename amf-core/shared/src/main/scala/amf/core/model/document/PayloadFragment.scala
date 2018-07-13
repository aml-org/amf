package amf.core.model.document

import amf.core.metamodel.document.{FragmentModel, PayloadFragmentModel}
import amf.core.model.StrField
import amf.core.model.domain.{DataNode, ScalarNode}
import amf.core.parser.{Annotations, Fields}

case class PayloadFragment(fields: Fields = Fields(), annotations: Annotations = Annotations()) extends Fragment {
  override def encodes: DataNode = fields(PayloadFragmentModel.Encodes)

  /** Meta data for the document */
  override def meta = FragmentModel

  def withMediaType(mediaType: String): this.type = {
    set(PayloadFragmentModel.MediaType, mediaType)
    this
  }

  def mediaType: StrField = fields.field(PayloadFragmentModel.MediaType)
}

object PayloadFragment {
  private def apply(payload: DataNode): PayloadFragment = apply().withEncodes(payload)

  private def apply(): PayloadFragment =
    PayloadFragment(Fields(), Annotations())
      .withId("http://test.com/payload")
      .withLocation("http://test.com/payload")

  def apply(payload: String, mediaType: String): PayloadFragment = apply(ScalarNode(payload, None), mediaType)

  def apply(payload: DataNode, mediaType: String): PayloadFragment =
    apply(payload)
      .withMediaType(mediaType)
}
