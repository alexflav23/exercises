package model.db

import com.outworkers.phantom.connectors.{CassandraConnection, ContactPoint}
import com.outworkers.phantom.dsl._

object Connector {
  lazy val default = ContactPoint.local.noHeartbeat().keySpace("rps_store")
}

class StoreDatabase(override val connector: CassandraConnection) extends Database[StoreDatabase](connector) {
  object products extends StoreProducts with Connector
  object productsByCategory extends ProductsByCategory with Connector
}

object StoreDatabase extends StoreDatabase(connector = Connector.default)

trait ProdDb extends DatabaseProvider[StoreDatabase] {
  override def database: StoreDatabase = StoreDatabase
}