package model.db

import com.outworkers.phantom.dsl._
import model.domain.StoreProduct

abstract class StoreProducts extends CassandraTable[StoreProducts, StoreProduct] with RootConnector {
  object id extends UUIDColumn(this) with PartitionKey
  object categories extends SetColumn[String](this)
  object name extends StringColumn(this)
  object attributes extends MapColumn[String, String](this)
}
