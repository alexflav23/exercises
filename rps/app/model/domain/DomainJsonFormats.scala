package model.domain

import play.api.libs.json._

/**
  * A list of implicit formats generated using the [[Json]] macro implementation.
  * We are keeping the implicits grouped here. We could also store the implicits
  * nested under the companion objects of their respective target case classes,
  * but in this instance we will store them here for simplicity.
  */
trait DomainJsonFormats {

  implicit val categoryFormat = Json.format[Category]
  implicit val categoriesFormat = Json.format[Categories]
  implicit val categoryDistributionFormat = Json.format[CategoryDistribution]
  implicit val attributesFormat = Json.format[Attributes]
  implicit val productFormat = Json.format[StoreProduct]

}
