# editors

UI framework for engineering data editors.

## Motivation

Information and communications technologies bring up a lot of power to solve real world problems by providing network infrastructure and computing power to process and share data. Nevertheless, very often humans need to manually enter or edit data, using editors.

To my experience, developing an editor is one of the less productive engineering task. It seems that capitalizing editors code is challenging, probably because several abstraction layers and concerns are involved and entangled: user interface, feedback logic (completion, validation, etc.), data transmission from and to the server and data domain. Consider for instance the following HTML fragment defining a form to enter a shop item description:

<pre><code>
&lt;form <span title="data transmission" style="background-color: #FFFCDD;">method=POST action="/item"</span>&gt;
  &lt;input <span title="data transmission" style="background-color: #FFFCDD;">name=name</span> <span title="data domain" style="background-color: #DCF7F3;">type=text</span> <span title="user interface" style="background-color: #FFD8D8;">placeholder="Name"</span> <span title="feedback logic" style="background-color: #F5A2A2;">required</span>&gt;<span title="user interface" style="background-color: #FFD8D8;">&lt;br&gt;</span>
  &lt;input <span title="data transmission" style="background-color: #FFFCDD;">name=category</span> <span title="data domain" style="background-color: #DCF7F3;">type=text</span> <span title="user interface" style="background-color: #FFD8D8;">placeholder="Category"</span> <span title="feedback logic" style="background-color: #F5A2A2;">datalist=categories</span>&gt;<span title="user interface" style="background-color: #FFD8D8;">&lt;br&gt;</span>
  <span title="feedback logic" style="background-color: #F5A2A2;">&lt;datalist id=categories&gt;</span>
    <span title="feedback logic" style="background-color: #F5A2A2;">&lt;option value=food&gt;</span>
    <span title="feedback logic" style="background-color: #F5A2A2;">&lt;option value=garden&gt;</span>
    <span title="feedback logic" style="background-color: #F5A2A2;">&lt;option value=home&gt;</span>
  <span title="feedback logic" style="background-color: #F5A2A2;">&lt;/datalist&gt;</span>
  &lt;input <span title="data transmission" style="background-color: #FFFCDD;">name=price</span> <span title="data domain" style="background-color: #DCF7F3;">type=number</span> <span title="user interface" style="background-color: #FFD8D8;">placeholder="Price (in Euros)"</span> <span title="feedback logic" style="background-color: #F5A2A2;">min=1 required</span>&gt;<span title="user interface" style="background-color: #FFD8D8;">&lt;br&gt;</span>
  <span title="user interface" style="background-color: #FFD8D8;">&lt;input type=submit /&gt;</span>
&lt;/form&gt;
</code></pre>

The different concerns present in the markup have been highlighted to illustrate how they are entangled. But, besides being entangled, they also are redundant with other parts of the code, on the server side. Consider for instance the corresponding form binder in Play:

<pre><code>
val itemForm = Form(mapping(
  <span title="data transmission" style="background-color: #FFFCDD;">"name"</span> -> <span title="data domain" style="background-color: #DCF7F3;">nonEmptyText</span>,
  <span title="data transmission" style="background-color: #FFFCDD;">"category"</span> -> <span title="data domain" style="background-color: #DCF7F3;">optional(nonEmptyText)</span>,
  <span title="data transmission" style="background-color: #FFFCDD;">"price"</span> -> <span title="data domain" style="background-color: #DCF7F3;">double</span><span title="feedback logic" style="background-color: #F5A2A2;">(min = 1)</span>
)(Item.apply, Item.unapply))

<span title="data domain" style="background-color: #DCF7F3;">case class Item(name: String, category: Option[String], price: Double)</span>
</code></pre>

It turns out that our markup duplicates information already present in the form binder definition (field names, input types and validation constraints), but also adds its own value (form look and placeholders). Similarly, it turns out that the form binder definition almost duplicates information already present in the case class definition (field types) but also adds its own value (validation constraints).

How to reuse the information that is already available in the case class definition in order to define the form binder by supplying only the missing parts? Similarly, how to reuse the information already available in the form binder in order to define the form markup by supplying only the missing parts?

What if we could engineer a data editor just from the data domain definition? We could then bootstrap our application very quickly and progressively enhance it with the missing information related to the validation and presentation concerns.

## Quick start

This section shows how to engineer the same editor as in the previous section, but without duplicating information. We will start by generating a basic editor and show how to enhance it progressively.

### Generate a basic editor

Back to our example, a basic editor can be generated with just the following code:

```scala
case class Item(name: String, category: Option[String], price: Double)
val editor = Editor[Item]
```

The HTML markup of the editor can be retrieved using its `ui` field, and the binding process can be achieved using its `bind` member:

```scala
object Items extends Controller {
  val form = Action {
    Ok(editor.ui(routes.Items.create))
  }
  val create = Action { request =>
    editor.bind(request) match {
      case Success(item) => Ok(s"Item created: ${item.name}")
      case Failure(errors) => BadRequest(s"Error: $errors")
    }
  }
}
```

The library inspects the `Item` type at compile-time (using a macro) and try to generate a data binder and form input fields, based on the case class fields, their name and their type. The generated markup looks like the following:

```html
<form method="POST" action="/items">
  <input name="name" type="text" placeholder="Name" />
  <input name="category" type="text" placeholder="Category" />
  <input name="price" type="number" placeholder="Price" />
  <input type="submit" />
</form>
```

And the Play form binder would look like the following:

```scala
Form(mapping(
  "name" -> nonEmptyText,
  "category" -> optional(nonEmptyText),
  "price" -> double
))
```

### Customize the keys used by the input fields

The generator derives the input field names from the case class member names. However, in some cases you want to customize the input names used in the form (for technical reasons, usually). This is possible by defining the following implicit instance of `Keys[Item]`:

```scala
implicit val itemKeys = Keys.fields[Item](name = "item_name", category = "item_category", price = "item_price")
```

The `Keys.fields[Item]` function (generated by a macro) takes named parameters with the same names as the case class `Item` fields. It is possible to customize only one field key:

```scala
implicit val itemKeys = Keys.fields[Item](category = "section")
```

### Add validation rules

In order to avoid to reinvent the wheel, you can use whatever validation system you want. The examples below use the new Play! validation API.

Data validation is considered to be part of the data mapping process: data can be mapped from form inputs only if their content is valid. By default, no validation rule is applied: the system just tries to coerce the input to the target field type.

The following code defines the validation rule for the `price` field:

```scala
implicit def itemMapping(implicit Double: Mapping[Double]) =
  Mapping.fields[Item](price = Double >>> min(1.0))
```

The other fields are left untouched, by default the UI generator assumes that fields of type `Option[_]` are not mandatory.

### Add presentational information

#### Choose a theme

The `Ui[A]` typeclass is responsible of generating the editor HTML markup. Default instances are provided in the `Ui` companion object, generating input fields for common types. Using another theme is as simple as importing it, consider for instance the following code using a theme producing markup compatible with [Twitter Bootstrap](http://getbootstrap.com/):

```scala
import editors.themes.twitterBootstrap._
```

#### Set per field presentational information

Each theme can have its own characteristics (use labels or placeholders, use a hint text, etc.), and, within a given theme, you can set per field information (label, hint, etc.):

```scala
implicit val itemPresentation =
  Presentation.fields[Item](
    name = FieldPresentation(placeholder = "Name"),
    category = FieldPresentation(placeholder = "Category"),
    price = FieldPresentation(placeholder = "Price (in Euros)")
  )
```

#### Add field specific markup

By default the UI markup generation is type directed (using the `FieldUi[A]` typeclass) but you can completely customize the markup for just one field or for your whole type:

```scala
implicit def itemUi(implicit Keys: Keys[Item]) =
  Ui.fields[Item](
    name = FieldUi { field =>
      inputText(field, "datalist" -> "categories") ++ <datalist id="categories">
        <option value="food" />
        <option value="garden" />
        <option value="home" />
      </datalist>
    }
  )
```

The above code uses UI combinators functions to generate markup consistent with the selected theme. Each theme is responsible of defining such combinators.

## Installation

```scala
libraryDependencies += "com.julienrf" %% "editors" % "0.1-SNAPSHOT"
```

## Build from sources

Clone the source code and run sbt from the repository root directory:

```sh
$ git clone git@github.com:julienrf/editors.git
$ cd editors
$ sbt
```

Run the tests:

```
> test
```

Or run a sample:

```
> project sample-backlog
[sample-backlog] $ run
```
