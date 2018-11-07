package amf.core.parser

import org.yaml.model.{YComment, YDocument}

abstract class ParsedDocument

case class SyamlParsedDocument(document: YDocument, comment: Option[YComment] = None) extends ParsedDocument
