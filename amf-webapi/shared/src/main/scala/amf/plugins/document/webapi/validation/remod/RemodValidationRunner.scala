package amf.plugins.document.webapi.validation.remod

import amf.ProfileName
import amf.client.remod.amfcore.plugins.validate.{AMFValidatePlugin, ValidationOptions}
import amf.core.model.document.BaseUnit
import amf.core.validation.AMFValidationReport
import amf.plugins.document.webapi.resolution.pipelines.ValidationResolutionPipeline

import scala.concurrent.{ExecutionContext, Future}

trait RemodValidationRunner {

  def run(unit: BaseUnit)(implicit executionContext: ExecutionContext): Future[AMFValidationReport]

  protected def emptyReport(unit: BaseUnit, profile: ProfileName): AMFValidationReport = AMFValidationReport.empty(unit.id, profile)
}

object RemodValidationRunnerFactory {
  // TODO: isResolved is kept for backwards compatibility
  def build(plugins: Seq[AMFValidatePlugin], options: ValidationOptions): RemodValidationRunner = {

    FailFastValidationRunner(plugins, options)
  }
}

case class ValidationResolutionRunner(runner: RemodValidationRunner, profile: ProfileName)
    extends RemodValidationRunner {

  // TODO: should clone BaseUnit if there are plugins that DON'T need resolution. This is tricky
  override def run(unit: BaseUnit)(implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val resolvedUnit = ValidationResolutionPipeline(profile, unit)
    runner.run(resolvedUnit)
  }
}

case class ParallelValidationRunner(plugins: Seq[AMFValidatePlugin], options: ValidationOptions)
    extends RemodValidationRunner {
  override def run(unit: BaseUnit)(implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    Future.sequence(plugins.map(_.validate(unit, options))).map { reports =>
      mergeReports(reports, emptyReport(unit, options.profileName))
    }
  }

  private def mergeReports(reports: Seq[AMFValidationReport], initial: AMFValidationReport) =
    reports.foldLeft(initial) { (acc, curr) =>
      acc.merge(curr)
    }
}

case class FailFastValidationRunner(plugins: Seq[AMFValidatePlugin], options: ValidationOptions)
    extends RemodValidationRunner {
  override def run(unit: BaseUnit)(implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {
    val emptyFutureReport = Future.successful(emptyReport(unit, options.profileName))
    plugins.foldLeft(emptyFutureReport) { (acc, curr) =>
      acc.flatMap { report =>
        failFastGuard(report) {
          curr.validate(unit, options).map(nextReport => report.merge(nextReport))
        }
      }
    }
  }

  private def failFastGuard(report: AMFValidationReport)(
      toRun: => Future[AMFValidationReport]): Future[AMFValidationReport] = {
    if (report.conforms) toRun else Future.successful(report)
  }
}
