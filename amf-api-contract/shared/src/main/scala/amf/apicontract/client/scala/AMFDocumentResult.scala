package amf.apicontract.client.scala

import amf.core.client.scala.AMFResult
import amf.core.client.scala.model.document.{Document, Module}
import amf.core.client.scala.validation.AMFValidationResult

/**
  * An [[AMFResult]] where the parsing result is a [[Document]]
  *
  * @param document the Document parsed
  * @param report The [[AMFValidationReport]] from parsing the Document
  * @see [[AMFBaseUnitClient.parseDocument parseDocument]]
  */
class AMFDocumentResult(val document: Document, results: Seq[AMFValidationResult]) extends AMFResult(document, results)

/**
  * An [[AMFResult]] where the parsing result is a library a.k.a. [[Module]]
  *
  * @param library The library parsed
  * @param report The Seq [[AMFValidationReport]] from parsing the library
  * @see [[AMFBaseUnitClient.parseLibrary parseLibrary]]
  */
class AMFLibraryResult(val library: Module, results: Seq[AMFValidationResult]) extends AMFResult(library, results)
