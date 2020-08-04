package com.nbasnet.nxml

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

trait XmlReads[T] {
  val isCollection: Boolean = false

  def read(xml: NodeSeq): Either[ParseFailed, T]

  def read(xml: NodeSeq, propField: String): Either[ParseFailed, T] = {
    if (isCollection) read(xml \\ propField)
    else read(xml \ propField)
  }
}

object XmlReads extends DefaultXmlReads with LowPriorityXmlReads {
  def apply[T](reads: NodeSeq => Either[ParseFailed, T]): XmlReads[T] = (xml: NodeSeq) => reads(xml)

  def readFromMacro[T](
    xml: NodeSeq,
    field: XmlField,
    configField: Option[XmlField],
    xmlSettings: Option[XmlSettings] = None
  )(implicit reads: XmlReads[T]): Either[ParseFailed, T] = {
    val updatedField = field.overrideField(configField)
    val xmlFieldPath = updatedField.xmlPathName

    val normalizedPath = xmlSettings match {
      case Some(settings) => settings.pathNormalizer(xmlFieldPath, updatedField.nameSpace)
      case _              => xmlFieldPath
    }

    if (updatedField.isNodeValue) reads.read(xml)
    else reads.read(xml, normalizedPath)
  }
}

trait DefaultXmlReads {

  def parse[T](node: NodeSeq)(p: NodeSeq => T)(implicit
    cTag: ClassTag[T]
  ): Either[ParseFailed, T] = {
    Try(p(node)) match {
      case Success(value) => Right(value)
      case Failure(ex) =>
        Left(ParseFailed(s"Parse failed: ${ex.getMessage}", node, cTag.runtimeClass.getSimpleName))
    }
  }

  implicit case object stringReads extends XmlReads[String] {
    def read(xml: NodeSeq): Either[ParseFailed, String] = Right(xml.text.trim)
  }

  implicit case object boolReads extends XmlReads[Boolean] {

    def read(xml: NodeSeq): Either[ParseFailed, Boolean] = {
      if (xml.text == "true") Right(true)
      else if (xml.text == "false") Right(false)
      else Left(ParseFailed("Parse failed: invalid value for bool", xml, "boolean"))
    }
  }

  implicit val intReads: XmlReads[Int] = XmlReads[Int](parse(_)(_.text.trim.toInt))
  implicit val longReads: XmlReads[Long] = XmlReads[Long](parse(_)(_.text.trim.toLong))
  implicit val floatReads: XmlReads[Float] = XmlReads[Float](parse(_)(_.text.trim.toFloat))
  implicit val doubleReads: XmlReads[Double] = XmlReads[Double](parse(_)(_.text.trim.toDouble))

}

trait LowPriorityXmlReads {

  implicit def optReads[T](implicit reads: XmlReads[T]): XmlReads[Option[T]] = {
    (xml: NodeSeq) =>
      {
        if (xml.text == null || xml.text.isBlank) Right(None)
        else reads.read(xml).map(Some(_))
      }
  }

  implicit def seqReads[T](implicit reads: XmlReads[T]): XmlReads[Seq[T]] =
    new XmlReads[Seq[T]] {
      override val isCollection: Boolean = true

      def read(xml: NodeSeq): Either[ParseFailed, Seq[T]] = {
        xml.foldLeft[Either[ParseFailed, Seq[T]]](Right(Nil)) { (parseAcc, nextNode) =>
          parseAcc match {
            case Right(parsed) => reads.read(nextNode).map(n => parsed :+ n)
            case Left(err)     => Left(err)
          }
        }
      }
    }

  implicit def listReads[T](implicit reads: XmlReads[T]): XmlReads[List[T]] = {
    (xml: NodeSeq) => seqReads[T].read(xml).map(_.toList)
  }

  implicit def vectorReads[T](implicit reads: XmlReads[T]): XmlReads[Vector[T]] = {
    (xml: NodeSeq) => seqReads[T].read(xml).map(_.toVector)
  }
}
