package amf.shapes.internal.domain.resolution.shape_normalization
import amf.core.client.scala.model.domain.Shape

// lastExit:              It is possible to have many nested unions. This is the id of the last union to be analyzed by the RecursionAnalyzer about this recursion 
//                        This means that an exit (a non recursive member) should exist for this recursion to be valid
// relativeToUnion:       This is the id of the union for which the recursion is currently considered. 
//                        It is used to considerate recursions that will be handled later when analyzing if exists an exit in the actual union
// relativeToUnionMember: This is the id of the member of the union under analysis for which the union was found
// generator:             The Shape that appeared twice during traversal

case class UnionRecursionRecord(
    lastExit: String,
    var relativeToUnion: String,
    var relativeToUnionMember: String,
    generator: Shape
)
