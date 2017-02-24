package model.service

import com.danielasfregola.randomdatagenerator.RandomDataGenerator
import model.domain.{Category, StoreProduct}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers, OptionValues}
import service.InMemoryStoreService

import scala.concurrent.ExecutionContext.Implicits.global

class InMemoryServiceTest extends FlatSpec with ScalaFutures with Matchers with OptionValues with RandomDataGenerator {
  val service = new InMemoryStoreService()


  it should "add a product to the store and retrieve it" in {
    val product = random[StoreProduct]

    val chain = for {
      _ <- service.addProduct(product)
      byTitle <- service.getProduct(product.title)
    } yield byTitle

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldEqual product
    }
  }

  it should "associate a product with a category" in {
    val product = random[StoreProduct]
    val cat = random[Category]

    val chain = for {
      _ <- service.addProduct(product)
      byTitle <- service.getProduct(product.title)
      addCategory <- service.addProductToCategory(product.title, cat)
      byTitle2 <- service.getProduct(product.title)
      byCategory <- service.productTitlesForCategory(cat.value)
    } yield (byTitle, byTitle2, byCategory)

    whenReady(chain) { case (beforeUpdate, afterUpdate, productTitles) =>
      beforeUpdate shouldBe defined
      beforeUpdate.value shouldEqual product

      afterUpdate shouldBe defined
      info("The updated product should have one more category")
      afterUpdate.value.categories.values should contain theSameElementsAs (product.categories.values + cat)

      productTitles should contain (product.title)
    }
  }
}
