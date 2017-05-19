package controllers

import com.outworkers.phantom.dsl.context
import model.domain.{Category, DomainJsonFormats}
import play.api.libs.json._
import play.api.mvc._
import service.StoreFront

trait ProductApiController extends DomainJsonFormats {

  def storage: StoreFront

  /**
    * Retrieves a map of categories alongside a count of products in each category.
    * @return A Map where each entry is a [[Category]] together with the total count of products in that category.
    */
  def getCategoryDistribution: Action[AnyContent] = Action.async { req =>
    storage.getCategoryDistribution.map(data => Results.Ok(Json.toJson(data)))
  }

  /**
    * Requirement 2:
    * A tuple of categories where the right hand set is the set of distinct product titles that correspond to that [[Category]].
    * @return A map of categories with the product titles for each.
    */
  def categoriesWithProductTitles(cat: String): Action[AnyContent] = Action.async { req =>
    storage.productTitlesForCategory(cat).map(data => Results.Ok(Json.toJson(data)))
  }

  /**
    * Requirements 3.
    * Retrieves a product alongside the full set of attributes and categories for a product.
    * @param title The title to find a product by.
    * @return A future wrapping an optional result, where [[None]] means no product with the respective table was found.
    */
  def getProduct(title: String): Action[AnyContent] = Action.async { req =>
    storage.getProduct(title) map {
      case Some(product) => Results.Ok(Json.toJson(product))
      case None => Results.NoContent
    }
  }

  /**
    * Requirements 4.
    * Associates a product with a new category.
    * @param title The title to find a product by.
    * @param category The category to associate the product with.
    * @return A future wrapping an HTTP Response, failures will automatically result in an HTTP 500 error.
    */
  def addProductToCategory(title: String, category: String): Action[AnyContent] = Action.async { req =>
    storage.addProductToCategory(title, Category(category)).map(data => Results.Ok("Updated successfully"))
  }
}
