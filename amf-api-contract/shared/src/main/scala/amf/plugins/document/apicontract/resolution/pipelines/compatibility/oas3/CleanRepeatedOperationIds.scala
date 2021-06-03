package amf.plugins.document.apicontract.resolution.pipelines.compatibility.oas3

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.apicontract.models.Operation

class CleanRepeatedOperationIds() extends TransformationStep {
  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit =
    try {
      val operations           = getOperationsFromModel(model)
      val repeatedOperationIds = operationsWithRepeatedIds(operations)
      repeatedOperationIds.foreach(addDistinctOperationIds)
      model
    } catch {
      case _: Throwable => model
    }

  private def addDistinctOperationIds(operations: Seq[Operation]): Unit =
    operations.zipWithIndex.foreach {
      case (operation, index) => operation.withName(operation.name + s"_${index}")
    }

  private def operationsWithRepeatedIds(operations: Seq[Operation]): Seq[Seq[Operation]] =
    operations
      .filter(x => x.name.option().isDefined)
      .groupBy(_.name.value())
      .filter(isRepeated)
      .values
      .toSeq

  private def isRepeated(tuple: (String, Seq[Operation])): Boolean = tuple._2.size > 1

  private def getOperationsFromModel[T <: BaseUnit](model: T): Seq[Operation] =
    model
      .iterator()
      .collect {
        case operation: Operation => operation
      }
      .toSeq
}
