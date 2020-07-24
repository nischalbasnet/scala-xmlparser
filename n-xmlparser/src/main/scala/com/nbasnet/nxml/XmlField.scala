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

  val xmlPathName: String = {
    val path = nameWithoutNameSpace.getOrElse("")
    if (isChildNodeValue) path else s"@$path"
  }

  def overrideField(config: Option[XmlField]): XmlField = {
    XmlField(
      name = config.map(_.name).getOrElse(this.name),
      isNodeValue = config.map(_.isNodeValue).getOrElse(this.isNodeValue),
      isChildNodeValue = config.map(_.isChildNodeValue).getOrElse(this.isChildNodeValue),
      nameSpace = config.flatMap(_.nameSpace).orElse(this.nameSpace)
    )
  }

}

case class XmlSettings(
  nameNormalizer: String => String,
  nameSpace: Option[String] = None
)
