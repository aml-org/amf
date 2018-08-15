package amf.client.plugins

import amf.client.remote.Content
import amf.core.model.document.BaseUnit
import amf.core.parser.{ParsedDocument, ReferenceKind}

trait AMFFeaturePlugin extends AMFPlugin {

  /**
    * Callback invoked for every client invocation to the parsing logic.
    *
    * @param url URL of the top level document being parsed
    * @param mediaType optional media type passed in the invocation
    */
  def onBeginParsingInvocation(url: String, mediaType: Option[String]): Unit = ()

  /**
    * Callback invoked for every linked document being parsed as result of the client invocation of the parser
    * @param url URL of the documen being parsed
    * @param content Raw content being parsed after fetching it fromt the remote location
    * @param referenceKind Type of reference for the content
    */
  def onBeginDocumentParsing(url: String, content: Content, referenceKind: ReferenceKind): Content =
    content

  /**
    * Callback invoked for every successful syntax AST being parsed for any linked document
    * @param url URL of the document being parsed
    * @param ast Parsed document AST
    */
  def onSyntaxParsed(url: String, ast: ParsedDocument): ParsedDocument = ast

  /**
    * Callabck being invoked for every successful domain model being parsed for any linked document
    * @param url URL of the document being parsed
    * @param unit Parsed domain unit
    */
  def onModelParsed(url: String, unit: BaseUnit): BaseUnit = unit

  /**
    * Callback being invoked after every successful parser invocation containing the top level domain unit being parsed
    * @param url URL of the top level document being parsed
    * @param unit parsed domain unit for the top level document
    */
  def onFinishedParsingInvocation(url: String, unit: BaseUnit): BaseUnit = unit

}
