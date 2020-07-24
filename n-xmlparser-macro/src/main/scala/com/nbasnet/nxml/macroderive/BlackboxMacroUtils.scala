package com.nbasnet.nxml.macroderive

import scala.reflect.NameTransformer
import scala.reflect.macros.blackbox

abstract class BlackboxMacroUtils(val c: blackbox.Context) {
  import c.universe._

  protected case class FieldInfo(name: String, tpe: Type, annotations: List[Annotation])

  protected def encodeTerm(name: String): TermName = TermName(NameTransformer.encode(name))

  protected def isCaseClass(tpe: Type): Boolean =
    tpe.typeSymbol.isClass && tpe.typeSymbol.asClass.isCaseClass

  protected def isModuleClass(tpe: Type): Boolean =
    tpe.typeSymbol.isClass && tpe.typeSymbol.asClass.isModuleClass

  protected def primaryConstructor(tpe: Type): MethodSymbol = {
    tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor =>
        if (!m.isPublic)
          c.error(
            c.enclosingPosition,
            s"Only classes with public primary constructor are supported. Found: $tpe"
          )
        m
    }.get
  }

  protected def caseClassFieldsTypes(tpe: Type): List[FieldInfo] = {
    val paramLists = primaryConstructor(tpe).paramLists
    val params = paramLists.head

    if (paramLists.size > 1)
      c.error(
        c.enclosingPosition,
        s"Only classes with at least one parameter list are supported. Found: $tpe"
      )

    params.foreach { p =>
      if (!p.isPublic)
        c.error(
          c.enclosingPosition,
          s"Only classes with all public constructor arguments are supported. Found: $tpe"
        )
    }

    params.map { p =>
      FieldInfo(p.name.toTermName.decodedName.toString, p.infoIn(tpe), p.annotations)
    }
  }
}
