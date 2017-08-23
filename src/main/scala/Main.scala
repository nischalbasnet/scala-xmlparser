import com.nischal.services.file.xml.XmlParser
import com.nischal.services.validation._
import models.{Catalog, ItemSize}

import scala.xml._


/**
  * Created by nbasnet on 5/20/17.
  */
/**
  * Testing conversion
  */
object Main extends App
{

  xmlTest()
  validationTest()

  def xmlTest(): Unit =
  {
    val xmlFile = this.getClass.getResource("product-test.xml")
    val xmlSource = XML.load(xmlFile.getPath)

    val catalog = XmlParser.parse(xmlSource)(Catalog.xmlRead)

    catalog.foreach(println)

    val newImg = XmlParser.parse(xmlSource)(ItemSize.xmlRead)

    newImg.foreach(println)
  }

  def validationTest(): Unit =
  {
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
)
{

  import com.nischal.services.validation.DefaultValidations._

  private val validationsNew: Seq[IFieldValidator[_]] = Seq(
    StringField("user_id", user_id,
      StartsWith("usr") and Length(min = 5)
    ),
    IntField("age", age,
      InBetween(min = 2)
    ),
    OptField[String]("group", group,
      StartsWith("aa") and Length(max = 4, min = 2)
    )
  )

  def validate(group: Option[String] = None): Seq[String] = validationsNew.flatMap(_.validate(group))
}
