package com.nbasnet.nxml

import com.nbasnet.models.ColorSwatch
import com.nbasnet.nxml.macroderive.DeriveConfigs.{FieldConfig, XmlConfig}
import com.nbasnet.nxml.macroderive.{ChildNodeValue, DeriveXml, XmlFieldName, XmlNameSpace}
import specs.BaseTestSpec

import scala.xml._

object TestData {

  val contactXmlWithNamSpace: Elem = XML.loadString(
    """<?xml version="1.0" encoding="UTF-8"?>
      <cont:contact xmlns:id="x" id:cont="http://sssit.org/contact-us">
        <cont:name>Some Name</cont:name>
        <cont:age>44</cont:age>
        <cont:company>SOME.org</cont:company>
        <cont:phone>(100) 100-1010</cont:phone>
        <cont:company-name>Some Company</cont:company-name>
        <cont:is-admin>true</cont:is-admin>
      </cont:contact>"""
  )

  case class ContactWithAnnotation(
    @XmlNameSpace("x") cont: String,
    @ChildNodeValue name: String,
    @ChildNodeValue age: Int,
    @ChildNodeValue company: String,
    @ChildNodeValue phone: String,
    @ChildNodeValue @XmlFieldName("company-name") company_name: String,
    @ChildNodeValue @XmlFieldName("is-admin") is_admin: Boolean
  )

  object ContactWithAnnotation {

    implicit val reads: XmlReads[ContactWithAnnotation] =
      DeriveXml.deriveReader[ContactWithAnnotation]()
  }

  case class ContactWithConfig(
    cont: String,
    @XmlFieldName("_name") name: String,
    age: Int,
    company: String,
    phone: String,
    company_name: String,
    is_admin: Boolean
  )

  object ContactWithConfig {

    implicit val reads: XmlReads[ContactWithConfig] =
      DeriveXml.deriveReader[ContactWithConfig](
        XmlConfig(XmlSettings.camelCaseToHyphen()),
        //although through annotation name was set as _name this should take precedence
        FieldConfig("cont", XmlField(nameSpace = Some("x"))),
        FieldConfig("name", XmlField(name = Some("name"), isChildNodeValue = true)),
        FieldConfig("age", XmlField(isChildNodeValue = true)),
        FieldConfig("company", XmlField(isChildNodeValue = true)),
        FieldConfig("phone", XmlField(isChildNodeValue = true)),
        FieldConfig("company_name", XmlField(isChildNodeValue = true)),
        FieldConfig("is_admin", XmlField(isChildNodeValue = true))
      )
  }

}

class XmlReadsSpec extends BaseTestSpec {

  "XmlReads.read" should {

    "convert xml node to expected type using handwritten reader" in {
      val xml = <color_swatch image="red_cardigan.jpg">Red</color_swatch>

      XmlTransformer.read(xml)(ColorSwatch.xmlReader) mustBe Right(ColorSwatch(
        image = "red_cardigan.jpg",
        color = "Red"
      ))
    }

    "convert xml node to expected type using derived reader" in {
      val xml = <color_swatch image="red_cardigan.jpg">Red</color_swatch>

      XmlTransformer.read(xml)(ColorSwatch.implReader) mustBe Right(ColorSwatch(
        image = "red_cardigan.jpg",
        color = "Red"
      ))
    }

    "return parser error if reader is unable to convert xml node" in {
      val xml = <color_swatch xmlns:id="id" id:img="red_cardigan.jpg" id:i="green_baron">
        Red
      </color_swatch>
      val reader = DeriveXml.deriveReader[ColorSwatch](
        FieldConfig("image", XmlField(name = Some("img"), nameSpace = Some("id")))
      )

      XmlTransformer.read[ColorSwatch](xml)(reader) mustBe Right(ColorSwatch(
        image = "red_cardigan.jpg",
        color = "Red"
      ))
    }

    "convert xml with namespace with annotation derived reader" in {
      val transformed =
        XmlTransformer.read[TestData.ContactWithAnnotation](TestData.contactXmlWithNamSpace)

      transformed mustBe Right(TestData.ContactWithAnnotation(
        cont = "http://sssit.org/contact-us",
        name = "Some Name",
        age = 44,
        company = "SOME.org",
        phone = "(100) 100-1010",
        company_name = "Some Company",
        is_admin = true
      ))
    }

    "convert xml with namespace with config passed to derived reader" in {
      val transformed =
        XmlTransformer.read[TestData.ContactWithConfig](TestData.contactXmlWithNamSpace)

      transformed mustBe Right(TestData.ContactWithConfig(
        cont = "http://sssit.org/contact-us",
        name = "Some Name",
        age = 44,
        company = "SOME.org",
        phone = "(100) 100-1010",
        company_name = "Some Company",
        is_admin = true
      ))

    }
  }

}
