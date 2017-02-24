package service

import javax.inject.{Inject, Singleton}

import com.outworkers.phantom.dsl.context
import com.typesafe.scalalogging.StrictLogging
import model.db.ProdDb
import model.domain.{Category, CategoryDistribution, StoreProduct}
import play.api.Environment
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

@Singleton
class DatabaseProductService @Inject()(
  environment: Environment,
  applicationLifecycle: ApplicationLifecycle
)(
  implicit exec: ExecutionContext
) extends StoreFront with StrictLogging with ProdDb {

  /**
    * Retrieves a map of categories alongside a count of products in each category.
    *
    * @return A Map where each entry is a [[Category]] together with the total count of products in that category.
    */
  override def getCategoryDistribution: Future[CategoryDistribution] = {
    database.productsByCategory.distribution() map (res => CategoryDistribution(res.toMap))
  }

  /**
    * Requirement 2:
    * A tuple of categories where the right hand set is the set of distinct product titles that correspond to that [[Category]].
    *
    * @return A map of categories with the product titles for each.
    */
  override def productTitlesForCategory(title: String): Future[Set[String]] = {
    database.productsByCategory.productsByCategory(Category(title)) map (_.map(_.title).toSet)
  }

  /**
    * Retrieves a product alongside the full set of attributes and categories.
    *
    * @param title The title of the product to retrieve from storage.
    * @return A future wrapping an Option, [[Some]] if a product with the given title exists, [[None]] otherwise.
    */
  override def getProduct(title: String): Future[Option[StoreProduct]] = {
    database.products.findByTitle(title)
  }

  override def addProductToCategory(title: String, category: Category): Future[Unit] = {
    getProduct(title) flatMap {
      case Some(product) =>
        for {
          cat <- database.productsByCategory.store(category, product)
          prod <- database.products.addCategory(title, category)
        } yield ()

      case None => Future.failed(new Exception(s"Could not find product with title $title") with NoStackTrace)
    }
  }

  override def addProduct(product: StoreProduct): Future[Unit] = {
    for {
      _ <- database.products.store(product)
      cats <- Future.sequence(product.categories.values.map(c => database.productsByCategory.store(c, product)))
    } yield ()

  }
}
