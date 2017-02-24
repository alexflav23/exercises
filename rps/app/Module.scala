import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import service.InMemoryStoreService

class Module(
  environment: Environment,
  configuration: Configuration
) extends AbstractModule {

  override def configure() = {
    bind(classOf[InMemoryStoreService]).asEagerSingleton()
  }
}