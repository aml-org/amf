package amf.client.environment

import amf.client.remod.AMFResult
import amf.core.model.document.{Document, Module}
import amf.core.validation.AMFValidationReport

/**
  * An {@link amf.client.remod.AMFResult} where the parsing result is a {@link amf.core.model.document.Document}
  * @param document the Document parsed
  * @param report The {@link amf.core.validation.AMFValidationReport} from parsing the Document
  * @see {@linkplain AMFClient.parseDocument parseDocument}
  */
class AMFDocumentResult(val document: Document, report: AMFValidationReport) extends AMFResult(document, report)

/**
  * An {@link amf.client.remod.AMFResult} where the parsing result is a library a.k.a. {@link amf.core.model.document.Module}
  * @param library The library parsed
  * @param report The {@link amf.core.validation.AMFValidationReport} from parsing the library
  * @see {@linkplain AMFClient.parseLibrary parseLibrary}
  */
class AMFLibraryResult(val library: Module, report: AMFValidationReport) extends AMFResult(library, report)
