package amf.shapes.internal.spec.jsonldschema.instance.model

import amf.core.client.scala.model.document.Document
import amf.core.internal.parser.domain.{Annotations, Fields}

class JsonLDInstanceDocument(override val fields: Fields, override val annotations: Annotations)
    extends Document(fields: Fields, annotations: Annotations) {}
