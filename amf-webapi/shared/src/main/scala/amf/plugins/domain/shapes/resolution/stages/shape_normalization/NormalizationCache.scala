package amf.plugins.domain.shapes.resolution.stages.shape_normalization
import amf.core.errorhandling.ErrorHandler
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import amf.validations.ResolutionSideValidations.InvalidTypeInheritanceWarningSpecification
import amf.{ProfileName, Raml08Profile}

import scala.collection.mutable

private[plugins] class NormalizationContext(final val errorHandler: ErrorHandler,
                                            final val keepEditingInfo: Boolean,
                                            final val profile: ProfileName,
                                            val cache: NormalizationCache = NormalizationCache()) {

  val isRaml08: Boolean                        = profile.equals(Raml08Profile)
  private val minShapeClass: MinShapeAlgorithm = new MinShapeAlgorithm()(this)

  def minShape(derivedShape: Shape, superShape: Shape): Shape = {

    try {
      minShapeClass.computeMinShape(derivedShape, superShape)
    } catch {
      case e: InheritanceIncompatibleShapeError =>
        errorHandler.violation(
          InvalidTypeInheritanceWarningSpecification,
          derivedShape.id,
          e.property.orElse(Some(ShapeModel.Inherits.value.iri())),
          e.getMessage,
          e.position,
          e.location
        )
        derivedShape
      case other: Throwable =>
        errorHandler.violation(
          ResolutionValidation,
          derivedShape.id,
          Some(ShapeModel.Inherits.value.iri()),
          other.getMessage,
          derivedShape.position(),
          derivedShape.location()
        )
        derivedShape
    }
  }

}

private[shape_normalization] case class NormalizationCache() {
  def addClosures(closureShapes: Seq[Shape], s: Shape): Unit = {
    closureShapes.foreach { c =>
      cacheClosure(c.id, s)
    }
  }

  def cacheClosure(id: String, array: Shape): this.type = {
    cacheWithClosures.get(id) match {
      case Some(seq) => cacheWithClosures.update(id, seq :+ array)
      case _         => cacheWithClosures.update(id, Seq(array))
    }
    this
  }

  def updateFixPointsAndClosures(canonical: Shape, withoutCaching: Boolean): Unit = {
    // First check if the shape has itself as closure or fixpoint target (because of it still not in the cache)
    canonical.closureShapes.find(_.id == canonical.id) match {
      case Some(x) =>
        canonical.closureShapes.remove(x)
        canonical.closureShapes.add(canonical)
      case _ => // Nothing to do
    }
    canonical match {
      case r: RecursiveShape if r.fixpointTarget.isDefined && r.fixpointTarget.get.id == canonical.id =>
        r.withFixpointTarget(canonical)
      case _ => // Ignore
    }

    // Then if the flag of caching is enabled, check and update other shapes
    if (!withoutCaching) {
      updateRecursiveTargets(canonical)
      cacheWithClosures.get(canonical.id) match {
        case Some(seq) =>
          seq.foreach { s =>
            s.closureShapes.find(clo => clo.id == canonical.id && clo != canonical) match {
              case Some(clo) =>
                s.closureShapes.remove(clo)
                s.closureShapes += canonical
              case _ => // ignore
            }
          }
        case _ => // ignore
      }
    }
  }

  def updateRecursiveTargets(newShape: Shape): NormalizationCache = {
    fixPointCache.values.flatten
      .filter(_.fixpointTarget.exists(_.id == newShape.id))
      .foreach(_.withFixpointTarget(newShape))
    this
  }

  def removeIfPresent(shape: Shape): this.type = {
    get(shape.id) match {
      case Some(s) if s.equals(shape) => cache.remove(shape.id)
      case _                          =>
    }
    this
  }

  def registerMapping(id: String, alias: String): this.type = {
    mappings.get(alias) match {
      case Some(a) =>
        mappings.remove(alias)
        mappings.put(id, a)
      case _ =>
        mappings.put(id, alias)
        fixPointCache.get(id).foreach { seq =>
          fixPointCache.remove(id)
          fixPointCache.put(alias, seq.map(_.withFixPoint(alias)))
        }
    }
    this
  }

  private val cache = mutable.Map[String, Shape]()
  /* Shape in the closure -> Shape that in s.closures contains the shape */
  private val cacheWithClosures = mutable.Map[String, Seq[Shape]]()

  private val fixPointCache = mutable.Map[String, Seq[RecursiveShape]]()
  private val mappings      = mutable.Map[String, String]()

  private def registerFixPoint(r: RecursiveShape): RecursiveShape = {
    r.fixpoint.option().foreach { fp =>
      val alias = mappings.get(fp)
      fixPointCache.get(fp) match {
        case Some(s) =>
          val shapes = s :+ r
          val newAlias = alias.fold({
            fp
          })(a => {
            shapes.foreach(_.withFixPoint(a))
            fixPointCache.remove(fp)
            a
          })
          fixPointCache.put(newAlias, shapes)

        case _ =>
          alias.fold({
            fixPointCache.put(fp, Seq(r))
          })(a => {
            r.withFixPoint(a)
            fixPointCache.put(a, Seq(r))
          })
      }
    }
    r
  }

  def +(shape: Shape): this.type = {
    shape match {
      case r: RecursiveShape =>
        registerFixPoint(r)
      case _ =>
    }
    cache.put(shape.id, shape)
    this
  }

  def get(id: String): Option[Shape] = cache.get(id)

  def exists(id: String): Boolean = cache.contains(id)
}
