package com.nbasnet.nxml

trait XmlWrites[T] {
  def write(tag: String, m: T): String
}

object XmlWrites extends DefaultXmlWrites with LowPriorityWrites {

  def tagXml[T](t: String, v: T): String = {
    if (t.startsWith("@")) s"${t.stripPrefix("@")}=$v"
    else if (t.isEmpty) s"$v"
    else s"<$t>$v</$t>"
  }

  def apply[T](writes: (String, T) => String): XmlWrites[T] = (t: String, m: T) => writes(t, m)
  def default[T]: XmlWrites[T] = tagXml[T]

  def writeFromMacro[T](
    v: T,
    field: XmlField,
    configField: Option[XmlField],
    xmlSettings: Option[XmlSettings] = None
  )(implicit writes: XmlWrites[T]): String = {
    val updatedField = field.overrideField(configField)
    val xmlFieldPath = updatedField.xmlPathName

    val normalizedPath = xmlSettings match {
      case Some(settings) => settings.pathNormalizer(xmlFieldPath, updatedField.nameSpace)
      case _              => xmlFieldPath
    }

    if (updatedField.isNodeValue) writes.write("", v)
    else writes.write(normalizedPath, v)
  }
}

trait DefaultXmlWrites {
  import XmlWrites._

  implicit val stringWrites: XmlWrites[String] = default[String]
  implicit val boolWrites: XmlWrites[Boolean] = default[Boolean]
  implicit val intWrites: XmlWrites[Int] = default[Int]
  implicit val longWrites: XmlWrites[Long] = default[Long]
  implicit val floatWrites: XmlWrites[Float] = default[Float]
  implicit val doubleWrites: XmlWrites[Double] = default[Double]
  implicit val bigDecimalWrites: XmlWrites[BigDecimal] = default[BigDecimal]
}

trait LowPriorityWrites {
  import XmlWrites._

  implicit def optWrites[T](implicit writes: XmlWrites[T]): XmlWrites[Option[T]] = {
    (t: String, m: Option[T]) =>
      {
        m match {
          case Some(v) => writes.write(t, v)
          case None    => tagXml(t, "")
        }
      }
  }

  implicit def seqReads[T](implicit writes: XmlWrites[T]): XmlWrites[Seq[T]] =
    (tag: String, m: Seq[T]) => {
      m.map(writes.write(tag, _)).mkString("\n")
    }
}
