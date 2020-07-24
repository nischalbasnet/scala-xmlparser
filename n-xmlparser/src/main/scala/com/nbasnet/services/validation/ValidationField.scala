package com.nbasnet.services.validation

/**
  * Class to perform field validation for class properties
  *
  * @param fieldName   : name of the property
  * @param fieldValue  : value of the property to validate
  * @param validations : set of validation to run against fieldValue
  * @tparam T : Type for the validator classes
  */
case class ValidationField[T](
  fieldName: String,
  fieldValue: T,
  validations: ValidationContainer[T]
) extends IFieldValidator[T] {

  def validate(group: Option[String] = None): Vector[String] = {
    val errorMessage = scala.collection.mutable.ListBuffer[String]()

    validations.validations.foreach { fieldValidation =>
      val inValidationGroup = group match {
        case Some(s: String) => fieldValidation.groups.contains(s)
        case _               => true
      }

      if (inValidationGroup && !fieldValidation.validate(fieldValue)) {
        errorMessage += fieldValidation.getError(fieldName)
      }
    }

    errorMessage.toVector
  }
}

/**
  * Optional field validator
  * @param fieldName   : name of the property
  * @param fieldValue  : value of the property to validate
  * @param validations : set of validation to run against fieldValue
  * @tparam T : Type for the validator classes
  */
case class OptValidationField[T](
  override val fieldName: String,
  fieldValue: Option[T],
  override val validations: ValidationContainer[T]
) extends IFieldValidator[T] {

  override def validate(group: Option[String]): Vector[String] = {
    //validate only if the value is present
    if (fieldValue == null || fieldValue.isEmpty) Vector.empty
    else {
      val validator = ValidationField(
        fieldName,
        fieldValue.get,
        validations
      )
      validator.validate(group)
    }
  }
}
