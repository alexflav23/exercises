package model.db

import com.outworkers.phantom.connectors.ContactPoint
import com.outworkers.phantom.dsl._

object Connector {
  lazy val default = ContactPoint.local.noHeartbeat().keySpace("rps_store")
}

class StoreDatabase(override val connector: KeySpaceDef) extends Database[StoreDatabase](connector) {
  object products extends StoreProducts with Connector
  object products_by_category extends ProductsByCategory with Connector
}

object StoreDatabase extends StoreDatabase(connector = Connector.default)