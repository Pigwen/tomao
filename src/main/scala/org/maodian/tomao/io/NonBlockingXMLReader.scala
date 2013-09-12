/**
 *
 */
package org.maodian.tomao.io

/**
 * @author cole wen
 *
 */
trait NonBlockingXMLReader {
  /*def getFeature(name: String): Boolean
  def setFeature(name: String, value: Boolean): Unit*/
}

object NonBlockingXMLReader {
  val FeatureNamespace = "http://xml.org/sax/features/namespaces";
  val FeatureNamespacePrefixes = "http://xml.org/sax/features/namespace-prefixes";
  val FeatureCommentsAllowed = "http://mina.apache.org/vysper/features/comments-allowed";
  val FeatureRestartAllowed = "http://mina.apache.org/vysper/features/restart-allowed";
  val PropertyRestartQName = "http://mina.apache.org/vysper/properties/restart-qname";
}

class DefaultNonBlockingXMLReader extends NonBlockingXMLReader {
  import NonBlockingXMLReader._

  val features = Map[String, Boolean](
    FeatureNamespace -> true,
    FeatureNamespacePrefixes -> false,
    FeatureCommentsAllowed -> true,
    FeatureRestartAllowed -> true)

  val properties = Map[String, Any](
    PropertyRestartQName -> "stream:stream")
}

class XMLParser {
  
}
