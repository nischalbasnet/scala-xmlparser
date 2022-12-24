package com.nbasnet.nxml

case class XmlField(
  name: Option[String] = None,
  isNodeValue: Boolean = false,
  isChildNodeValue: Boolean = false,
  nameSpace: Option[String] = None
) {

  val nameWithoutNameSpace: Option[String] = {
    for {
      ns <- nameSpace
      nm <- name
    } yield nm.replace(s"$ns:", "")
  }.orElse(name)

  def xmlPathName: String = {
    //default behaviour is to remove the namespace when reading, but can be modified by using
    //XmlSettings
    val path = nameWithoutNameSpace.getOrElse("")
    if (isChildNodeValue) path
    else if (nameSpace.isDefined) s"@{${nameSpace.get}}$path"
    else s"@$path"
  }

  def overrideField(config: Option[XmlField]): XmlField = {
    XmlField(
      name = config.flatMap(_.name).orElse(this.name),
      isNodeValue = config.map(_.isNodeValue).getOrElse(this.isNodeValue),
      isChildNodeValue = config.map(_.isChildNodeValue).getOrElse(this.isChildNodeValue),
      nameSpace = config.flatMap(_.nameSpace).orElse(this.nameSpace)
    )
  }

}

trait XmlSettings {

  /**
    * takes in name and the namespace of the field and normalizes it
    * @return normalized name
    */
  def pathNormalizer: (String, Option[String]) => String
  def fieldNormalizer: (String, Option[String]) => String

  /**
    * namespace to use if the field/object does not define its own
    */
  def nameSpace: Option[String]
}

object XmlSettings {

  def apply(
    pathNameNormalizer: (String, Option[String]) => String,
    fieldNameNormalizer: (String, Option[String]) => String,
    namespace: Option[String] = None
  ): XmlSettings =
    new XmlSettings {
      val pathNormalizer: (String, Option[String]) => String = pathNameNormalizer
      val fieldNormalizer: (String, Option[String]) => String = fieldNameNormalizer
      val nameSpace: Option[String] = namespace
    }

  def camelCaseToHyphen(nameSpace: Option[String] = None): XmlSettings =
    apply(
      pathNameNormalizer = (name, _) => name.replaceAll("_", "-"),
      fieldNameNormalizer = (name, _) => name.replaceAll("-", "_"),
      namespace = nameSpace
    )

}
