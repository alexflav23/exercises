package model.domain

case class Category(value: String)

/**
  * A list of (name -> value) string attributes for every product.
  * @param map A map of attributes, that guarantees we don't have duplicate values for a given attribute.
  */
case class Attributes(map: Map[String, String]) {

  /**
    * Adds a new attribute to the map of attributes.
    * @param other The new category to add to the set.
    * @return A new instance of [[Attributes]] with the new attribute added.
    *         If the attribute name is not unique, the map will "upsert" e.g it will update
    *         the value of a given attribute key.
    */
  def +(other: (String, String)): Attributes = copy(map + other)
}

object Attributes {
  def empty: Attributes = Attributes(Map.empty)
}

/**
  * In here we are storing a list of categories for a product.
  * We could simply use a set, but we are wrapping it into a separate class
  * just in case we may choose to add some functionality at a later stage.
  *
  * We use a Set because we want to make sure we have no duplicates in the data structure.
  * @param values The set of values to use for categories.
  */
case class Categories(values: Set[Category]) {
  def categories: Set[String] = values.map(_.value)

  /**
    * Adds a new category to the set of categories.
    * @param other The new category to add to the set.
    * @return A new instance of [[Categories]] with the new category appended. If the category is not unique
    *         it will not be appended.
    */
  def +(other: Category): Categories = copy(values = values + other)
}

object Categories {

  def empty: Categories = Categories(Set.empty[Category])

  /**
    * Helper to allow us to parse a structure from the database or storage easily.
    * @param set A set of strings that get persisted to the DB where each string represents a category.
    * @return A [[Categories]] instance corresponding to the input set.
    */
  def set(set: Set[String]): Categories = Categories(set.map(Category.apply))

  def apply(cat: Category): Categories = Categories(Set(cat))
}

case class CategoryDistribution(map: Map[String, Long])

/**
  * A simple product definition, containing the basic set of information about a product.
  * @param title The title of the product.
  * @param attributes The attributes map of the underlying product.
  */
case class StoreProduct(
  title: String,
  attributes: Attributes = Attributes.empty,
  categories: Categories = Categories.empty
)
