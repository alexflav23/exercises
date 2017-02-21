package controllers

import model.domain.{Category, StoreProduct}
import java.util.UUID

import scala.concurrent.Future

trait StoreFront {

  /**
    * Retrieves a map of categories alongside a count of products in each category.
    * @return A Map where each entry is a [[Category]] together with the total count of products in that category.
    */
  def getCategoryDistribution: Future[Map[Category, Long]]

  /**
    * Requirement 2:
    * A tuple of categories where the right hand set is the set of distinct product titles that correspond to that [[Category]].
    * @return A map of categories with the product titles for each.
    */
  def categoriesWithProductTitles: Future[(Category, Set[String])]

  /**
    * Retrieves a product alongside the full set of attributes and categories.
    * @param id
    * @return
    */
  def getProduct(id: UUID): Future[Option[StoreProduct]]

  def addProductToCategory(name: String, category: Category): Future[Unit]

  def addProductToCategory(id: UUID, category: Category): Future[Unit]
}
