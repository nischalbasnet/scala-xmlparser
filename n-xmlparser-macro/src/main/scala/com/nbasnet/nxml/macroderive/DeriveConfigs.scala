package com.nbasnet.nxml.macroderive

import com.nbasnet.nxml.{XmlField, XmlSettings}

object DeriveConfigs {

  sealed trait XmlReadConfig

  sealed trait XmlWriteConfig

  case class XmlConfig(settings: XmlSettings) extends XmlReadConfig with XmlWriteConfig

  case class FieldConfig(name: String, field: XmlField) extends XmlReadConfig with XmlWriteConfig

}
