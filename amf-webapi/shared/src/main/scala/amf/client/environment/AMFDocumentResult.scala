package amf.client.environment

import amf.client.remod.AMFResult
import amf.core.model.document.{Document, Module}
import amf.core.validation.AMFValidationReport

class AMFDocumentResult(document: Document, report: AMFValidationReport) extends AMFResult(document, report)

class AMFLibraryResult(library: Module, report: AMFValidationReport) extends AMFResult(library, report)
