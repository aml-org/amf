package amf.shapes.internal.domain.resolution.shape_normalization

import amf.core.client.scala.model.domain.{RecursiveShape, Shape}

import scala.collection.mutable

private[shape_normalization] case class NormalizationCache() {

  private val cache = mutable.Map[String, Shape]()

  private val fixPointCache = mutable.Map[String, Seq[RecursiveShape]]()
  private val mappings      = mutable.Map[String, String]()

  def updateFixPoints(canonical: Shape, withoutCaching: Boolean): Unit = {

    canonical match {
      case r: RecursiveShape if r.fixpointTarget.isDefined && r.fixpointTarget.get.id == canonical.id =>
        r.withFixpointTarget(canonical)
      case _ => // Ignore
    }

    // Then if the flag of caching is enabled, check and update other shapes
    if (!withoutCaching) {
      updateRecursiveTargets(canonical)
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
