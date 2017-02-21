package model.db

import com.outworkers.phantom.dsl._
import model.domain.StoreProduct

abstract class ProductsByCategory extends CassandraTable[ProductsByCategory, StoreProduct] with RootConnector {

  object category extends StringColumn(this) with PartitionKey
  object product_id extends UUIDColumn(this) with PrimaryKey
  object name extends StringColumn(this)
  object attributes extends MapColumn[String, String](this)
}
