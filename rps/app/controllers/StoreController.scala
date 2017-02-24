package controllers

import javax.inject.{Inject, Singleton}

import model.domain.{Attributes, Categories, Category, StoreProduct}
import play.api.Environment
import play.api.inject.ApplicationLifecycle
import service.{InMemoryStoreService, StoreFront}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@Singleton
class StoreController @Inject()(
  env: Environment,
  lifecycle: ApplicationLifecycle
)  extends ProductApiController {

  override val storage: StoreFront = new InMemoryStoreService()

  def init(): Unit = {

    val p1 = StoreProduct(
      "i5 6400",
      Attributes(Map("speed" -> "3.3Ghz", "cache" -> "6M")),
      Categories(Category("processors"))
    )

    val p2 = StoreProduct(
      "i5 4460",
      Attributes(Map("speed" -> "3.2Ghz", "cache" -> "6M")),
      Categories(Category("processors"))
    )

    /*
        Geforce 1050 (memory=4GB, cores=768)

    Radeon R7 (memory=4GB,memory-type=DDR3)

    R7 250 (memory=1GB)
     */
    val g1 = StoreProduct(
      "Geforce 1050",
      Attributes(Map("memory" -> "4GB", "cores" -> "768")),
      Categories(Category("graphics_cards"))
    )

    val g2 = StoreProduct(
      "RadeonR7",
      Attributes(Map("memory" -> "4GB", "memory-type" -> "DDR3")),
      Categories(Category("graphics_cards"))
    )

    val g3 = StoreProduct(
      "R7 250",
      Attributes(Map("memory" -> "4GB", "cores" -> "768")),
      Categories(Category("graphics_cards"))
    )

    val products = List(p1, p2, g1, g2, g3)
    Await.result(Future.sequence(products.map(storage.addProduct)), 20.seconds)
  }

  init()
}
