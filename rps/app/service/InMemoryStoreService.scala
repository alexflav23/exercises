package service

import javax.inject.Singleton

import com.typesafe.scalalogging.StrictLogging
import model.domain.{Category, CategoryDistribution, StoreProduct}

import scala.collection.mutable.{Map => MutableMap}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

@Singleton
class InMemoryStoreService()(
  implicit exec: ExecutionContext
) extends StoreFront with  StrictLogging {

  val productByTitle: MutableMap[String, StoreProduct] = MutableMap.empty

  val productsByCategory: MutableMap[Category, List[StoreProduct]] = MutableMap.empty

  /**
    * Retrieves a map of categories alongside a count of products in each category.
    *
    * @return A Map where each entry is a [[Category]] together with the total count of products in that category.
    */
  override def getCategoryDistribution: Future[CategoryDistribution] = synchronized {
    Future.successful(
      CategoryDistribution(productsByCategory map { case (key, list) =>
        key.value -> list.distinct.size.toLong } toMap
      )
    )
  }

  /**
    * Retrieves a product alongside the full set of attributes and categories.
    * @param title The title of the product to look for.
    * @return
    */
  override def getProduct(title: String): Future[Option[StoreProduct]] = Future.successful(productByTitle.get(title))

  /**
    * Adds a product to a category, even if the category doens't exist yet.
    * This will validate and check if a product with the given title exists before adding it to a category.
    * It won't however check if the category exists, and instead add it anyway if it doesn't.
    * @param title
    * @param category
    * @return
    */
  override def addProductToCategory(title: String, category: Category): Future[Unit] = {
    synchronized {
      productByTitle.get(title) match {
        case Some(product) => {
          // if we found a product with the given title, add it to the category.
          val existing = productsByCategory.getOrElse(category, Nil)

          productsByCategory(category) = product :: existing
          productByTitle(product.title) = product.copy(categories = product.categories + category)

          Future.successful(())
        }
        case None => Future.failed(new Exception(s"Could not find a product with title $title"))
      }
    }
  }

  /**
    * Requirement 2:
    * A tuple of categories where the right hand set is the set of distinct product titles that correspond to that [[Category]].
    *
    * @return A map of categories with the product titles for each.
    */
  override def productTitlesForCategory(category: String): Future[Set[String]] = {
    productsByCategory.get(Category(category)) match {
      case Some(products) => Future.successful(products.map(_.title).toSet)
      case None => Future.failed(new Exception(s"Unrecognised category $category") with NoStackTrace)
    }
  }

  override def addProduct(product: StoreProduct): Future[Unit] = synchronized {
    productByTitle(product.title) = product
    product.categories.values.foreach(cat => {
      val list = productsByCategory.getOrElse(cat, Nil)
      productsByCategory(cat) = product :: list
    })

    Future.successful(())
  }
}
