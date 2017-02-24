package service

import model.domain.{Category, CategoryDistribution, StoreProduct}

import scala.concurrent.Future

trait StoreFront {

  /**
    * Retrieves a map of categories alongside a count of products in each category.
    * @return A Map where each entry is a [[Category]] together with the total count of products in that category.
    */
  def getCategoryDistribution: Future[CategoryDistribution]

  /**
    * Requirement 2:
    * A tuple of categories where the right hand set is the set of distinct product titles that correspond to that [[Category]].
    * @return A map of categories with the product titles for each.
    */
  def productTitlesForCategory(title: String): Future[Set[String]]

  /**
    * Retrieves a product alongside the full set of attributes and categories.
    * @param title The title of the product to retrieve from storage.
    * @return A future wrapping an Option, [[Some]] if a product with the given title exists, [[None]] otherwise.
    */
  def getProduct(title: String): Future[Option[StoreProduct]]

  def addProduct(product: StoreProduct): Future[Unit]

  /**
    * Adds a product to a category. Implementors of this method will check
    * that the product title belongs to an existing product.
    * @param title The title of the the product to add to a category.
    * @param category A category to add the product to.
    * @return A future that will succeed if the attribution is successful.
    */
  def addProductToCategory(title: String, category: Category): Future[Unit]
}
