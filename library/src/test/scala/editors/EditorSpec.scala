package editors

import org.specs2.mutable.Specification
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.mvc.Call
import editors.bakery.Application

object EditorSpec extends Specification with Editors with SimpleLook {

  "Editor" should {

    // TODO Move this into Editor companion object
    // Expansion of the `Editor[User]` macro call
    def `Editor[User]`()(implicit Ui: Ui[User], Mapping: Mapping[User]) = new Editor[User] {
      def ui = Ui.ui("")
      def bind(data: Map[String, Seq[String]]) = Mapping.bind("", data)
    }
    implicit def editor(implicit Ui: Ui[User], Mapping: Mapping[User]) = `Editor[User]`()

    "generate forms for record types" in {

      "automatically" in {
        editor.ui.contains(<input type="text" name="name" />) must beTrue
        editor.ui.contains(<input type="number" name="age" />) must beTrue
        editor.ui.length must equalTo (2)
      }

      "with custom presentation data" in {
        implicit val userKeys = Keys.`fields[User]`(name = "user_name", age = "user_age")
        editor.ui.contains(<input type="text" name="user_name" />) must beTrue
        editor.ui.contains(<input type="number" name="user_age" />) must beTrue
        editor.ui.length must equalTo (2)
      }

      "whole form" in {
        val form = editor.formUi(Call("POST", "/submit"))
        form.label must equalTo ("form")
        form.attribute("method").get.apply(0).text must equalTo ("POST")
        form.attribute("action").get.apply(0).text must equalTo ("/submit")
        form.descendant.contains(<input type="text" name="name" />) must beTrue
        form.descendant.contains(<input type="number" name="age" />) must beTrue
      }

      "form with customized field" in {
        implicit val userUi = Ui.`fields[User]`(age = Ui(key => Ui.input(key, "number") ++ <span>From 7 to 77.</span>))
        editor.ui.contains(<input type="text" name="name" />) must beTrue
        editor.ui.contains(<input type="number" name="age" />) must beTrue
        editor.ui.contains(<span>From 7 to 77.</span>) must beTrue
      }
    }

    "bind data from form submission" in {
      "successfully" in {
        editor.bind(Map("name" -> Seq("julien"), "age" -> Seq("28"))) must beSome (User("julien", 28))
      }
      "with failure" in {
        editor.bind(Map("name" -> Seq("julien"), "age" -> Seq("twenty-height"))) must beNone
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
