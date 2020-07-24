# scala-xmlparser
Read and write xml to and from class

### Example: 
#### For xml:
```scala
   val xml = <size description="Extra Large">
                <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
                <color_swatch image="black_cardigan.jpg">Black</color_swatch>
            </size>
```
#### Model classes:
```scala  
case class ItemSize(description: String, @ChildNodeValue @XmlFieldName("color_swatch") images: Seq[ItemImage])

case class ColorSwatch(image: String, color: String)
```
#### For each classes we need to define the XmlNodeReader class:
```scala
  import com.nbasnet.nxml.XmlReads

  implicit val xmlReader: XmlReads[ItemSize] = (xml: NodeSeq) => {
    for {
      description <- implicitly[XmlReads[String]].read(xml, "@description")
      images <- implicitly[XmlReads[Seq[ColorSwatch]]].read(xml, "color_swatch")
    } yield ItemSize(description = description, images = images)
  }
  
  implicit val xmlReader: XmlReads[ColorSwatch] = (xml: NodeSeq) => {
    for {
      image <- implicitly[XmlReads[String]].read(xml \ "@image")
      color <- implicitly[XmlReads[String]].read(xml)
    } yield ColorSwatch(image = image, color = color)
  }
```
#### OR use macro method to auto derive reader
```scala
  import com.nbasnet.nxml.macroderive._
  import com.nbasnet.nxml.{XmlField, XmlReads}

  implicit val reader: XmlReads[ItemSize] = DeriveXml.deriveReader[ItemSize]()
  
  implicit val reader: XmlReads[ColorSwatch] = DeriveXml.deriveReader[ColorSwatch](
    //configuration can be described using annotation or by passing config to driveReader method
    FieldConfig("color", XmlField(isNodeValue = true))
  )
```
#### Now to parse the xml:
```scala
  import com.nbasnet.nxml.XmlTransformer

  val parsedItemSize = XmlTransformer.read[ItemSize](xml)
  parsedItemSize.foreach(println)
```  
#### Which will produce output:
##### ItemSize(Extra Large,List(ItemImage(burgundy_cardigan.jpg,Burgundy), ItemImage(black_cardigan.jpg,Black)))

#### Derive reader configs:
| Annotation                  | Config class                                                    | Comment                               |
| --------------------------- | --------------------------------------------------------------- | ------------------------------------- |
| `@XmlFieldName("xml_name")` | `FieldConfig("fld_name", XmlField(name = Some("xml_name")))`    | uses `xml_name` when retrieving value |
| `@ChildNodeValue`           | `FieldConfig("fld_name", XmlField(isChildNodeValue = true))`    | get value from child node             |
| `@NodeValue`                | `FieldConfig("fld_name", XmlField(isNodeValue = true))`         | get value from current node           |
