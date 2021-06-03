package amf.client.environment

import amf.client.remod.AMFResult
import amf.core.model.document.{Document, Module}
import amf.core.validation.{AMFValidationReport, AMFValidationResult}

/**
  * An [[AMFResult]] where the parsing result is a [[Document]]
  * @param document the Document parsed
  * @param report The [[AMFValidationReport]] from parsing the Document
  * @see [[AMFClient.parseDocument parseDocument]]
  */
class AMFDocumentResult(val document: Document, results: Seq[AMFValidationResult]) extends AMFResult(document, results)

/**
  * An [[AMFResult]] where the parsing result is a library a.k.a. [[Module]]
  * @param library The library parsed
  * @param report The [[AMFValidationReport]] from parsing the library
  * @see [[AMFClient.parseLibrary parseLibrary]]
  */
class AMFLibraryResult(val library: Module, results: Seq[AMFValidationResult]) extends AMFResult(library, results)
