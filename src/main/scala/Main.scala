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

  val catalog = XmlParser.parse(xmlSource \\ "catalog")(Catalog.xmlRead)

  catalog.foreach(println)

  val images = XmlParser.parse(xmlSource \\ "size")(ItemSize.xmlRead)

  val imageNodes = XmlParser.write(images)(ItemSize.xmlWriter)

  println(NodeSeq.fromSeq(imageNodes))
}
