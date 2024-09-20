package amf.shapes.internal.validation.avro

import amf.core.client.scala.validation.AMFValidationResult

// AVRO Validators return some validations errors which seems to be incorrect. We are filtering them
object AvroKnownErrors {

  private val invalidDefaultValidation = Seq("[] not a ")
  private val invalidTypeValidationJvm = Seq("No type:", "\"type\":[")
  private val invalidTypeValidationJs  = Seq("unknown type: [")

  private val filteredValidations = Seq(invalidDefaultValidation, invalidTypeValidationJvm, invalidTypeValidationJs)

  def filterResults(results: Seq[AMFValidationResult]): Seq[AMFValidationResult] =
    results.filterNot(r => shouldFilterMessage(r.message))

  def shouldFilterMessage(message: String): Boolean =
    filteredValidations.exists(f => shouldFilterMessageWithFilter(message, f))

  private def shouldFilterMessageWithFilter(message: String, filter: Seq[String]): Boolean = filter.forall(message.contains)

}
