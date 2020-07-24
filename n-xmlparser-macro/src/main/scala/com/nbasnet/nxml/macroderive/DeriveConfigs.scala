package com.nbasnet.nxml.macroderive

import com.nbasnet.nxml.XmlField

object DeriveConfigs {

  sealed trait XmlReadConfig

  sealed trait XmlWriteConfig

  case class FieldConfig(
    name: String,
    field: XmlField
  ) extends XmlReadConfig
      with XmlWriteConfig

}
