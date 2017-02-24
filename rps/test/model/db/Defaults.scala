package model.db

import com.outworkers.phantom.connectors.ContactPoint
import com.outworkers.phantom.dsl.DatabaseProvider

object Defaults {
  val connector = ContactPoint.local.noHeartbeat().keySpace("rps_store")
}

object TestDb extends StoreDatabase(Defaults.connector)

object TestDbProvider extends DatabaseProvider[StoreDatabase] {
  override def database: StoreDatabase = TestDb
}