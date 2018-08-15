package amf.core.exception

class UnsupportedVendorException(val vendor: String)
    extends RuntimeException(s"Cannot parse document with specified vendor: $vendor")
