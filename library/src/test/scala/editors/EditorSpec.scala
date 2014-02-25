package editors

import org.specs2.mutable.Specification
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.Call
import editors.bakery.Application
import play.api.data.mapping.Success

object EditorSpec extends Specification {

  object SimpleEditor extends Editors with SimpleLook
  object BootstrapEditor extends Editors with TwitterBootstrapLook

  "Editor" should {

    "generate forms for record types" in {

      "automatically" in {
        val editor = SimpleEditor.Editor[User]
        editor.ui.contains(<input type="text" name="name" />) must beTrue
        editor.ui.contains(<input type="number" name="age" />) must beTrue
        editor.ui.length must equalTo (2)
      }

      "with custom keys" in {
        implicit val userKeys = Key.`fields[User]`(name = "user_name", age = "user_age")
        val editor = SimpleEditor.Editor[User]
        editor.ui.contains(<input type="text" name="user_name" />) must beTrue
        editor.ui.contains(<input type="number" name="user_age" />) must beTrue
        editor.ui.length must equalTo (2)
      }

      "with different looks" in {
        val editor = BootstrapEditor.Editor[User]
        (editor.ui \\ "input").contains(<input name="name" type="text" placeholder="Enter name" />) must beTrue
      }

      "with custom presentation data" in {
        implicit val userPresentation =
          BootstrapEditor.Presentation.`fields[User]`(
            name = BootstrapEditor.FieldPresentation("name", Some("Name:"), None, placeholder = Some("Enter your name"), Seq.empty) // TODO I want to just write `FieldPresentation(placeholder = "foo")`
          )
        val editor = BootstrapEditor.Editor[User]
        (editor.ui \\ "input").contains(<input name="name" type="text" placeholder="Enter your name" />) must beTrue
      }

      "whole form" in {
        val editor = SimpleEditor.Editor[User]
        val form = editor.formUi(Call("POST", "/submit"))
        form.label must equalTo ("form")
        form.attribute("method").get.apply(0).text must equalTo ("POST")
        form.attribute("action").get.apply(0).text must equalTo ("/submit")
        form.descendant.contains(<input type="text" name="name" />) must beTrue
        form.descendant.contains(<input type="number" name="age" />) must beTrue
      }

      "form with customized field" in {
        implicit val userUi = SimpleEditor.Ui.`fields[User]`(age = SimpleEditor.FieldUi(key => SimpleEditor.FieldUi.input(key, "number") ++ <span>From 7 to 77.</span>))
        val editor = SimpleEditor.Editor[User]
        editor.ui.contains(<input type="text" name="name" />) must beTrue
        editor.ui.contains(<input type="number" name="age" />) must beTrue
        editor.ui.contains(<span>From 7 to 77.</span>) must beTrue
      }
    }

    "bind data from form submission" in {
      val editor = SimpleEditor.Editor[User]
      "successfully" in {
        editor.bind(Map("name" -> Seq("julien"), "age" -> Seq("28"))) must beEqualTo (Success(User("julien", 28)))
      }
      "with failure" in {
        editor.bind(Map("name" -> Seq("julien"), "age" -> Seq("twenty-height"))).isFailure must beTrue
      }
      "from within a Play action" in {
        val submissionAction = editor.submission[AnyContent](data => Results.Ok)
        def request(data: (String, String)*) = FakeRequest().withFormUrlEncodedBody(data: _*)

        "successfully" in {
          val result = submissionAction(request("name" -> "julien", "age" -> "28"))
          status(result) must equalTo (OK)
        }

        "with failure" in {
          val result = submissionAction(request("name" -> "julien", "age" -> "twenty-height"))
          status(result) must equalTo (422)
        }
      }
    }

    // editor.foo must equalTo (???)

    // editor.bar must equalTo (???)

    "support validation constraints" in pending ("(TODO)")

    "handle bidirectional relations" in pending("(TODO)")

    "support feedback logic definition" in pending("(TODO)")

    "do something in an app" in {
      status(Application.showForm(FakeRequest())) must equalTo (200)
    }

  }

}
