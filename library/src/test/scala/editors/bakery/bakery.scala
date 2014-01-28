package editors.bakery

import play.api.mvc.{AnyContent, Action, Controller}
import scala.concurrent.stm.Ref
import editors.{TwitterBootstrapLook, Mapping, Editors}

sealed trait IngredientUnit
case object GramPerFlourKilogram extends IngredientUnit
case object GramPerWaterLiter extends IngredientUnit

case class Ingredient(name: String, amount: Double, unit: IngredientUnit)

case class Recipe(id: Long, name: String, description: String, ingredients: Seq[Ingredient], hydratation: Double, sourdoughRation: Double)

object State {
  def state = Ref(Seq.empty[Recipe])
}

object RecipeEditor extends Editors with TwitterBootstrapLook {

  // Expansions of `TwitterBootstrapLook.FieldData.of` macro
  def defaultIngredientNameFieldData = FieldData("name", Some("Name"), None, None, ???)
  def defaultIngredientAmountFieldData = FieldData("amount", Some("Amount"), None, None, ???)
  def defaultIngredientUnitFieldData = FieldData("unit", Some("Unit"), None, None, ???)

  // Expansion of the Editor.apply[Ingredient] macro
//  def ingredientEditor(
//                        name: FieldData = defaultIngredientNameFieldData,
//                        amount: FieldData = defaultIngredientAmountFieldData,
//                        unit: FieldData = defaultIngredientUnitFieldData)(implicit uiString: Ui[String], mappingString: Mapping[String], uiDouble: Ui[Double], mappingDouble: Mapping[Double], uiUnit: Ui[IngredientUnit], mappingUnit: Mapping[IngredientUnit]): Editor[Ingredient] = new Editor[Ingredient] {
//    def ui = look.append(uiString.ui(name), uiDouble.ui(amount), uiUnit.ui(unit))
//    def bind(data: Map[String, Seq[String]]) = for {
//      nameData <- data.get("name")
//      amountData <- data.get("amount")
//      unitData <- data.get("unit")
//      name <- mappingString.bind(nameData)
//      amount <- mappingDouble.bind(amountData)
//      unit <- mappingUnit.bind(unitData)
//    } yield Ingredient(name, amount, unit)
//  }
//
//  val editor: Editor[Ingredient] = ingredientEditor()

}

object Application extends Controller {

  val showForm = Action { implicit request =>
    Ok
  }

//  val submitForm = RecipeEditor.editor.submission[AnyContent] { recipe =>
//    State.state.single.transform(_ :+ recipe)
//    Redirect(routes.Application.listRecipes)
//  }
//
//  val listRecipes = Action {
//    Ok(State.state.single())
//  }
//
//  def showRecipe(id: Long) = Action {
//    State.state.single().find(_.id == id) match {
//      case Some(recipe) => Ok(recipe)
//      case None => NotFound
//    }
//  }

}
