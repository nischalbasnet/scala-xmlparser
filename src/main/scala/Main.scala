import com.nischal.services.file.xml.XmlParser
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

  val xmlFile = this.getClass.getResource("product-test.xml")
  val xmlSource = XML.load(xmlFile.getPath)

  val catalog = XmlParser.parse(xmlSource)(Catalog.xmlRead)

  catalog.foreach(println)

  val newImg = XmlParser.parse(xmlSource)(ItemSize.xmlRead)

  newImg.foreach(println)
}
