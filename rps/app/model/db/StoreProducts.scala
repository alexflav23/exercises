package model.db

import com.outworkers.phantom.dsl._
import model.domain.{Attributes, Categories, Category, StoreProduct}

import scala.concurrent.Future

abstract class StoreProducts extends CassandraTable[StoreProducts, StoreProduct] with RootConnector {
  object title extends StringColumn(this) with PartitionKey
  object categories extends SetColumn[String](this)
  object attributes extends MapColumn[String, String](this)

  override def fromRow(row: Row): StoreProduct = {
    StoreProduct(
      title = title(row),
      attributes = Attributes(attributes(row)),
      categories = Categories.set(categories(row))
    )
  }

  def addCategory(title: String, category: Category): Future[ResultSet] = {
    update.where(_.title eqs title).modify(_.categories add category.value).future()
  }

  def store(product: StoreProduct): Future[ResultSet] = {
    insert
      .value(_.title, product.title)
      .value(_.categories, product.categories.categories)
      .value(_.attributes, product.attributes.map)
      .future()
  }

  def findByTitle(title: String): Future[Option[StoreProduct]] = {
    select.where(_.title eqs title).one()
  }
}
