package model.db

import com.outworkers.phantom.dsl._
import model.domain.{Attributes, Categories, Category, StoreProduct}

import scala.concurrent.Future

abstract class ProductsByCategory extends CassandraTable[ProductsByCategory, StoreProduct] with RootConnector {

  object category extends StringColumn(this) with PartitionKey
  object title extends StringColumn(this) with PrimaryKey
  object categories extends SetColumn[String](this)
  object attributes extends MapColumn[String, String](this)

  override def fromRow(row: Row): StoreProduct = {
    StoreProduct(
      title = title(row),
      attributes = Attributes(attributes(row)),
      categories = Categories.set(categories(row))
    )
  }

  def store(category: Category, product: StoreProduct): Future[ResultSet] = {
    insert
      .value(_.category, category.value)
      .value(_.title, product.title)
      .value(_.categories, product.categories.categories)
      .value(_.attributes, product.attributes.map)
      .future()
  }

  private[db] def countForCategory(category: String): Future[Long] = {
    select.count.where(_.category eqs category).one() map (_.getOrElse(0L))
  }

  def productsByCategory(category: Category): Future[List[StoreProduct]] = {
    select.where(_.category eqs category.value).fetch()
  }

  def distribution(): Future[List[(String, Long)]] = {
    for {
      categories <- select(_.category).fetch()
      futures = categories.map { cat => countForCategory(cat) map (cat -> _) }
      counts <- Future.sequence(futures)
    } yield counts
  }
}
