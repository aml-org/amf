package amf.core.parser

import org.yaml.model.{YComment, YDocument}

abstract class ParsedDocument

case class SyamlParsedDocument(comment: Option[YComment] = None, document: YDocument) extends ParsedDocument
