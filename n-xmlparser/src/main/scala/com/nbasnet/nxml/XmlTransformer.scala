package com.nbasnet.nxml

import scala.xml.NodeSeq

case class ParseFailed(error: String, node: NodeSeq, expectedType: String)

trait XmlTransformer[T] extends XmlReads[T] with XmlWrites[T]

object XmlTransformer {

  def apply[T](
    reads: NodeSeq => Either[ParseFailed, T],
    writes: (String, T) => String
  ): XmlTransformer[T] =
    new XmlTransformer[T] {
      def write(tag: String, m: T): String = writes(tag, m)
      def read(xml: NodeSeq): Either[ParseFailed, T] = reads(xml)
    }

  def read[T](xml: NodeSeq)(implicit xmlReads: XmlReads[T]): Either[ParseFailed, T] =
    xmlReads.read(xml)

  def write[T](t: String, m: T)(implicit xmlWrites: XmlWrites[T]): String = xmlWrites.write(t, m)
}

trait DefaultXmlTransformers {

  implicit def tXmlTransformer[T](
    implicit
    reads: XmlReads[T],
    writes: XmlWrites[T]
  ): XmlTransformer[T] = XmlTransformer(reads.read, writes.write)

}
