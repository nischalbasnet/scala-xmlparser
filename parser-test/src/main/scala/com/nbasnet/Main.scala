package com.nbasnet

import com.nbasnet.models.{Catalog, ColorSwatch}
import com.nbasnet.services.validation.{IFieldValidator, OptValidationField, ValidationField}
import com.nbasnet.services.validation.DefaultValidations._
import com.nbasnet.nxml.XmlTransformer

import scala.xml._

/**
  * Created by nbasnet on 5/20/17.
  */
/**
  * Testing conversion
  */
object Main extends App {

  xmlTest()
  validationTest()

  def xmlTest(): Unit = {
//    val xmlFile = this.getClass.getResource("product-test.xml")
    val xmlSource = XML.load(
      "/Users/nbasnet/WorkSpace/scala/scala-xmlparser/n-xmlparser/src/main/resources/product-test.xml"
    )

    println("==============USING NEW READER==============")
    val newCatalog = XmlTransformer.read[Catalog](xmlSource)
    println(newCatalog)
    newCatalog.toOption.get.products.head.catalog_items.map(_.size).foreach(_.foreach(println))

    println("==============USING NEW WRITER==============")
    val color = ColorSwatch("img.jpb", "Red")
    println(XmlTransformer.write("color_swatch", color))
    println(XmlTransformer.write("catalog", newCatalog.toOption.get))
  }

  def validationTest(): Unit = {
    val usr = User("bat", "mat", 2)
    println("==============FIRST USER VALIDATION==============")
    usr.validate().foreach(println)

    val usr2 = User("usr_idds", "haa", 10, Some("homerr"))
    println("==============SECOND USER VALIDATION==============")
    usr2.validate().foreach(println)
  }
}

case class User(
  user_id: String,
  first_name: String,
  age: Int,
  group: Option[String] = None
) {

  private val validationsNew: Seq[IFieldValidator[_]] = Seq(
    ValidationField("user_id", user_id, StartsWith("usr") and Length(min = 5)),
    ValidationField("age", age, InBetween(min = 2)),
    OptValidationField[String]("group", group, StartsWith("aa") and Length(max = 4, min = 2))
  )

  def validate(group: Option[String] = None): Seq[String] =
    validationsNew.flatMap(_.validate(group))
}
