package amf.tools

// Case classes to accumulate results
case class DialectNodeMapping(name: String,
                              classTerm: String,
                              propertyMappings: List[DialectPropertyMapping],
                              isShape: Boolean)
