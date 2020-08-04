package com.nbasnet.nxml

import scala.xml._

sealed trait XmlPath {
  def path: String
}

object XmlPath {
  case class Attribute(path: String) extends XmlPath
  case class ChildNode(path: String) extends XmlPath
  case class NodeValue(path: String) extends XmlPath

  def read(xml: NodeSeq, path: XmlPath): String = {
    path match {
      case Attribute(pth) => (xml \ s"@$pth").text
      case ChildNode(pth) => (xml \ pth).text
      case NodeValue(_)   => xml.text
    }
  }

  def write(v: Any, path: XmlPath): String = {
    path match {
      case Attribute(pth) => s"$pth=$v"
      case ChildNode(pth) => s"<$pth>$v</$pth>"
      case NodeValue(_)   => s"$v"
    }
  }
}
