package com.nischal.services.file.xml

import scala.xml.{Node, NodeSeq}

/**
  * Created by nbasnet on 5/20/17.
  */

/**
  * Object containing xml parsing function
  */
object XmlParser
{
  /**
    * Converts Node input to type T using xml node reader class for type T
    *
    * @param xmlInput
    * @param reader
    * @tparam T
    *
    * @return
    */
  def parse[T](xmlInput: Node)(reader: XmlNodeReader[T]): T =
  {
    reader.convertTo(xmlInput)
  }

  /**
    * Converts sequence Node input to sequence of T using xml node reader class for type T
    *
    * @param xmlNodes
    * @param reader
    * @tparam T
    *
    * @return
    */
  def parse[T](xmlNodes: NodeSeq)(reader: XmlNodeReader[T]): Seq[T] =
  {
    xmlNodes.map(cs => XmlParser.parse[T](cs)(reader))
  }

  /**
    * Converts sequence of T to sequence of Node
    *
    * @param in
    * @param writer
    * @tparam T
    *
    * @return
    */
  def write[T](in: Seq[T])(writer: XmlNodeWriter[T]): Seq[Node] =
  {
    NodeSeq.fromSeq(in.map(i => XmlParser.write[T](i)(writer)))
  }

  /**
    * Converts To to xml Node
    *
    * @param in
    * @param writer
    * @tparam T
    *
    * @return
    */
  def write[T](in: T)(writer: XmlNodeWriter[T]): Node =
  {
    writer.convertTo(in)
  }
}

/**
  * Contract to convert from FT to T
  *
  * @tparam FT
  * @tparam T
  */
trait IConvertTo[FT, T]
{
  /**
    * method that performs the conversion
    *
    * @param xml
    *
    * @return
    */
  def convertTo(xml: FT): T
}

/**
  * Converts xml Node to type T
  *
  * @param nodeName
  * @param read
  * @tparam T
  */
class XmlNodeReader[T](
  nodeName: String,
  read: (Node => T)
) extends IConvertTo[Node, T]
{
  def convertTo(xml: Node): T = read(xml)
}

/**
  * Converts type T to xml Node
  *
  * @param write
  * @tparam T
  */
class XmlNodeWriter[T](
  write: (T => Node)
) extends IConvertTo[T, Node]
{
  def convertTo(in: T): Node = write(in)
}