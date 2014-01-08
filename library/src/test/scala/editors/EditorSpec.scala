package editors

import org.specs2.mutable.Specification
import play.api.mvc._
import play.api.test.{FakeApplication, FakeRequest, WithApplication}
import play.api.test.Helpers._
import play.api.mvc.Call
import editors.bakery.Application

object EditorSpec extends Specification with Editors with SimpleLook {

  "Editor" should {

    case class User(name: String, age: Int)

    // Result of the expansion of the Editor.apply[User] macro
    def editorUser(name: FieldData = "name", age: FieldData = "age")(implicit uiString: Ui[String], mappingString: Mapping[String], uiInt: Ui[Int], mappingInt: Mapping[Int]): Editor[User] = new Editor[User] {
      def ui = look.append(uiString.ui(name), uiInt.ui(age))
      def bind(data: Map[String, Seq[String]]) = for {
        nameData <- data.get("name").orElse(Some(Seq.empty))
        ageData <- data.get("age").orElse(Some(Seq.empty))
        name <- mappingString.bind(nameData)
        age <- mappingInt.bind(ageData)
      } yield User(name, age)
    }

    "generate forms for record types" in {

      "automatically" in {
        val editor = editorUser() // Editor[User]()
        editor.ui.contains(<input type="text" name="name" />) must beTrue
        editor.ui.contains(<input type="number" name="age" />) must beTrue
        editor.ui.length must equalTo (2)
      }

      "with custom presentation data" in {
        val editor = editorUser(name = "user_name", age = "user_age") // Editor[User](name = "user_name", age = "user_age")
        editor.ui.contains(<input type="text" name="user_name" />) must beTrue
        editor.ui.contains(<input type="number" name="user_age" />) must beTrue
        editor.ui.length must equalTo (2)
      }

      "whole form" in {
        val editor = editorUser()
        val form = editor.formUi(Call("POST", "/submit"))
        form.label must equalTo ("form")
        form.attribute("method").get.apply(0).text must equalTo ("POST")
        form.attribute("action").get.apply(0).text must equalTo ("/submit")
        form.descendant.contains(<input type="text" name="name" />) must beTrue
        form.descendant.contains(<input type="number" name="age" />) must beTrue
      }
    }

    "bind data from form submission" in {
      val editor = editorUser()
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
