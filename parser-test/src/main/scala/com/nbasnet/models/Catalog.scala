package com.nbasnet.models

import com.nbasnet.nxml.macroderive._
import com.nbasnet.nxml.{XmlReads, XmlWrites}

import scala.xml.NodeSeq

/**
  * Created by nbasnet on 5/20/17.
  *
  * Classes for testing xml
  */

case class Catalog(
  @ChildNodeValue @XmlFieldName("product")
  products: Seq[CatalogProduct]
)

object Catalog {

  implicit val implReader: XmlReads[Catalog] = DeriveXml.deriveReader[Catalog]()
  implicit val writes: XmlWrites[Catalog] = DeriveXml.deriveWriter[Catalog]()

  val xmlReader: XmlReads[Catalog] = (xml: NodeSeq) => {
    for {
      products <- implicitly[XmlReads[Seq[CatalogProduct]]].read(xml, "product")
    } yield Catalog(products = products)
  }
}

case class CatalogProduct(
  description: String,
  product_image: String,
  @ChildNodeValue @XmlFieldName("catalog_item")
  catalog_items: Seq[CatalogItem]
)

object CatalogProduct {

  implicit val reader: XmlReads[CatalogProduct] = DeriveXml.deriveReader[CatalogProduct]()
  implicit val writes: XmlWrites[CatalogProduct] = DeriveXml.deriveWriter[CatalogProduct]()

  val xmlReader: XmlReads[CatalogProduct] = (xml: NodeSeq) => {
    for {
      description <- implicitly[XmlReads[String]].read(xml, "@description")
      product_image <- implicitly[XmlReads[String]].read(xml, "@product_image")
      catalog_items <- implicitly[XmlReads[Seq[CatalogItem]]].read(xml, "catalog_item")
    } yield CatalogProduct(
      description = description,
      product_image = product_image,
      catalog_items = catalog_items
    )
  }
}

case class CatalogItem(
  gender: String,
  @ChildNodeValue
  item_number: String,
  @ChildNodeValue
  price: Float,
  @ChildNodeValue
  size: Seq[ItemSize]
)

object CatalogItem {

  implicit val reader: XmlReads[CatalogItem] = DeriveXml.deriveReader[CatalogItem]()
  implicit val writes: XmlWrites[CatalogItem] = DeriveXml.deriveWriter[CatalogItem]()

  val xmlReader: XmlReads[CatalogItem] = (xml: NodeSeq) => {
    for {
      gender <- implicitly[XmlReads[String]].read(xml, "@gender")
      item_number <- implicitly[XmlReads[String]].read(xml, "item_number")
      price <- implicitly[XmlReads[Float]].read(xml, "price")
      size <- implicitly[XmlReads[Seq[ItemSize]]].read(xml, "size")
    } yield CatalogItem(gender = gender, item_number = item_number, price = price, size = size)
  }
}

case class ItemSize(
  description: String,
  @ChildNodeValue @XmlFieldName("color_swatch")
  images: Seq[ColorSwatch]
)

object ItemSize {

  implicit val reader: XmlReads[ItemSize] = DeriveXml.deriveReader[ItemSize]()
  implicit val writes: XmlWrites[ItemSize] = DeriveXml.deriveWriter[ItemSize]()

  val xmlReader: XmlReads[ItemSize] = (xml: NodeSeq) => {
    for {
      description <- implicitly[XmlReads[String]].read(xml, "@description")
      images <- implicitly[XmlReads[Seq[ColorSwatch]]].read(xml, "color_swatch")
    } yield ItemSize(description = description, images = images)
  }
}

case class ColorSwatch(
  image: String,
  @NodeValue
  color: String
)

object ColorSwatch {

  implicit val implReader: XmlReads[ColorSwatch] = DeriveXml.deriveReader[ColorSwatch]()

  implicit val writes: XmlWrites[ColorSwatch] = DeriveXml.deriveWriter[ColorSwatch]()

  val xmlReader: XmlReads[ColorSwatch] = (xml: NodeSeq) => {
    for {
      image <- implicitly[XmlReads[String]].read(xml \ "@image")
      color <- implicitly[XmlReads[String]].read(xml)
    } yield ColorSwatch(image = image, color = color)
  }
}
