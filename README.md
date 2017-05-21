# scala-xmlparser
Read and write xml to and from class

# Example: 
#### For xml:
```scala
   val xml = <size description="Extra Large">
                <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
                <color_swatch image="black_cardigan.jpg">Black</color_swatch>
            </size>
```
#### Model classes:
```scala  
case class ItemSize(description: String, images: Seq[ItemImage])

case class ItemImage(image: String, color: String)
```
#### For each classes we need to define the XmlNodeReader class:
```scala
  val xmlImageRead = new XmlNodeReader[ItemImage](
    nodeName = "color_swatch",
    read = n => {
      ItemImage(
        (n \ "@image").text,
        n.text
      )
    }
  )
  
  val xmlSizeRead = new XmlNodeReader[ItemSize](
    nodeName = "size",
    read = n => {
      val images = XmlParser.parse(n)(ItemImage.xmlRead)
      ItemSize(
        (n \ "@description").text,
        images
      )
    }
  )
```  
#### Now to parse the xml:
```scala
  val parsedItemSize = XmlParser.parse(xml)(xmlSizeRead)
  parsedItemSize.foreach(println)
```  
#### Which will produce output:
##### ItemSize(Extra Large,List(ItemImage(burgundy_cardigan.jpg,Burgundy), ItemImage(black_cardigan.jpg,Black)))
