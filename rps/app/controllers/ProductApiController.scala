package controllers

import java.util.UUID
import javax.inject.Singleton

import model.domain.StoreProduct

import scala.concurrent.Future

@Singleton
class ProductApiController {

  def getProduct(id: UUID): Future[Option[StoreProduct]]
}