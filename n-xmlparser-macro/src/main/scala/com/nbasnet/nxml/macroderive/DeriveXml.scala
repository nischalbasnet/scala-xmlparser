package com.nbasnet.nxml.macroderive

import com.nbasnet.nxml.macroderive.DeriveConfigs.{
  FieldConfig,
  XmlConfig,
  XmlReadConfig,
  XmlWriteConfig
}
import com.nbasnet.nxml.{XmlReads, XmlWrites}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

final class ChildNodeValue() extends scala.annotation.StaticAnnotation
final class NodeValue() extends scala.annotation.StaticAnnotation
final class XmlFieldName(name: String) extends scala.annotation.StaticAnnotation
final class XmlNameSpace(namespace: String) extends scala.annotation.StaticAnnotation

object DeriveXml {

  def deriveReader[T](settings: XmlReadConfig*): XmlReads[T] =
    macro DeriveXmlMacros.deriveReaderImpl[T]

  def deriveWriter[T](settings: XmlWriteConfig*): XmlWrites[T] =
    macro DeriveXmlMacros.deriveWriterImpl[T]
}

private[macroderive] class DeriveXmlMacros(override val c: blackbox.Context)
    extends BlackboxMacroUtils(c) {
  import c.universe._

  sealed trait MacroXmlConfig
  case class MacroFieldConfig(name: String, field: Tree) extends MacroXmlConfig
  case class MacroXmlSettings(settings: Tree) extends MacroXmlConfig

  def deriveReaderImpl[T: c.WeakTypeTag](settings: Tree*): Tree = {
//    println(deriveReaderInternal[T](settings))
    deriveReaderInternal[T](settings)
  }

  def deriveWriterImpl[T: c.WeakTypeTag](settings: Tree*): Tree = {
//    println(deriveWriterInternal[T](settings))
    deriveWriterInternal[T](settings)
  }

  protected def useValueOfNode(field: FieldInfo): Boolean = {
    scala.util.Try {
      field.annotations.map(_.tree).collectFirst {
        case q"new $name()" if name.tpe =:= typeOf[NodeValue] => true
      }.getOrElse(false)
    }.getOrElse(false)
  }

  protected def isChildNode(field: FieldInfo): Boolean = {
    scala.util.Try {
      field.annotations.map(_.tree).collectFirst {
        case q"new $name()" if name.tpe =:= typeOf[ChildNodeValue] => true
      }.getOrElse(false)
    }.getOrElse(false)
  }

  protected def annotationValue[T](
    field: FieldInfo
  )(implicit tt: TypeTag[T]): Option[String] = {
    field.annotations.map(_.tree).collectFirst {
      case q"new $name($arg)" if name.tpe =:= typeOf[T] =>
        val annValue = arg.toString()
        if (annValue.contains("$default$")) ""
        else annValue.stripPrefix("\"").stripSuffix("\"").trim
    }
  }

  def checkSetting[T: WeakTypeTag](setting: Tree): Boolean =
    weakTypeTag[T].tpe =:= c.typecheck(setting).tpe

  protected def getReadConfigs(settings: Seq[Tree]): Seq[MacroXmlConfig] = {
    settings.map {
      case q"$setting.apply(${name: String}, ${field})"
          if checkSetting[FieldConfig.type](setting) =>
        MacroFieldConfig(name, field)
      case q"$setting.apply(${settings})" if checkSetting[XmlConfig.type](setting) =>
        MacroXmlSettings(settings)
    }
  }

  protected def getWriteConfigs(settings: Seq[Tree]): Seq[MacroXmlConfig] = {
    settings.map {
      case q"$setting.apply(${name: String}, ${field})"
          if checkSetting[FieldConfig.type](setting) =>
        MacroFieldConfig(name, field)
    }
  }

  private def deriveReaderInternal[T: c.WeakTypeTag](settings: Seq[Tree]): c.universe.Tree = {
    val T = c.weakTypeOf[T]
    if (!isCaseClass(T)) c.error(c.enclosingPosition, s"not a case class: $T")

    val classFields = caseClassFieldsTypes(T)
    val xmlConfigs = getReadConfigs(settings)
    val fieldConfigs = xmlConfigs.collect { case f: MacroFieldConfig => f }
    val xmlSettings = xmlConfigs.collectFirst { case s: MacroXmlSettings => s }

    val fieldReads = classFields.map { fld =>
      val configFieldOpt = fieldConfigs.find(_.name == fld.name).map(_.field)
      val annoOrFieldPropName = annotationValue[XmlFieldName](fld).getOrElse(fld.name)
      val annoNameSpace = annotationValue[XmlNameSpace](fld)
      // format: off

      val macroXmlField = q"XmlField(${Some(annoOrFieldPropName)}, ${useValueOfNode(fld)}, ${isChildNode(fld)}, $annoNameSpace)"
      val macroReader = q"readFromMacro[${fld.tpe}](xml, $macroXmlField, ${configFieldOpt map c.untypecheck}, ${xmlSettings.map(_.settings) map c.untypecheck})"

      fq""" ${encodeTerm(fld.name)} <- $macroReader """
    }
    // format: on

    val applyConstructor = classFields.map { fld =>
      q""" ${encodeTerm(fld.name)} = ${encodeTerm(fld.name)} """
    }

    q"""
      {
        import com.nbasnet.nxml.{ParseFailed, XmlReads}
        import com.nbasnet.nxml.XmlField
        import com.nbasnet.nxml.XmlReads.readFromMacro
        import scala.xml.NodeSeq

        new XmlReads[$T]{
          def read(xml: NodeSeq): Either[ParseFailed, $T] = {
            for ( ..$fieldReads )
            yield new $T(..$applyConstructor)
          }
        }
      }
    """
  }

  //TODO: use settings when deriving xml
  private def deriveWriterInternal[T: c.WeakTypeTag](settings: Seq[Tree]): c.universe.Tree = {
    val T = c.weakTypeOf[T]
    if (!isCaseClass(T)) c.error(c.enclosingPosition, s"not a case class: $T")

    val classFields = caseClassFieldsTypes(T)

    val attributesFields = classFields.flatMap { fld =>
      if (!isChildNode(fld) && !useValueOfNode(fld)) {
        val attrName = annotationValue[XmlFieldName](fld).getOrElse(fld.name)
        val name = s"""$attrName=\""""
        val value = q"m.${encodeTerm(fld.name)}"
        val closeQuote = s"""\""""
        Some(q""" $name+$value+$closeQuote """)
      } else None
    }
    val emptyStr = " "
    val attributes = if (attributesFields.isEmpty) List(q"$emptyStr") else attributesFields

    val nodeValue = classFields.find(useValueOfNode).map { fld =>
      q"m.${encodeTerm(fld.name)}"
    }.getOrElse(q"$emptyStr")

    val childNodes = classFields.filter(isChildNode).map { fld =>
      val attrName = annotationValue[XmlFieldName](fld).getOrElse(fld.name)
      q" implicitly[XmlWrites[${fld.tpe}]].write($attrName, m.${encodeTerm(fld.name)}) "
    }

    // format: off
    q"""
      {
        import com.nbasnet.nxml.{XmlWrites}
        import scala.xml.Node

        new XmlWrites[$T] {
          def write(t: String, m: $T): String = {
            val childNodes = List(..$childNodes)
            "<" + t + List(..$attributes).mkString(" ", " ", "") + ">\n" + $nodeValue + childNodes.mkString("\n") + "\n</" + t + ">"
          }
        }
      }
    """
    // format: on
  }
}
