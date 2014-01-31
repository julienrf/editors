# Discussion

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

<pre><code>
val itemForm = Form(mapping(
  <span title="data transmission" style="background-color: #FFFCDD;">"name"</span> -> <span title="data domain" style="background-color: #DCF7F3;">nonEmptyText</span>,
  <span title="data transmission" style="background-color: #FFFCDD;">"category"</span> -> <span title="data domain" style="background-color: #DCF7F3;">optional(nonEmptyText)</span>,
  <span title="data transmission" style="background-color: #FFFCDD;">"price"</span> -> <span title="data domain" style="background-color: #DCF7F3;">double</span><span title="feedback logic" style="background-color: #F5A2A2;">(min = 1)</span>
)(Item.apply, Item.unapply))

<span title="data domain" style="background-color: #DCF7F3;">case class Item(name: String, category: Option[String], price: Double)</span>
</code></pre>

## Cake pattern

The cake pattern is useful as soon as you want to abstract over types: one layer of the cake can define an abstract type member that is implemented by another layer. A consequence for users is that all the code that uses stuff defined by the cake components must be placed within a surrounding trait or object (it can not be top-level).

To abstract over types, alternatives could be:

- type parameters: but that would add more noise at usage site and would require a wrapper trait as well in most cases,
- parameters + path dependent types: `def foo(dependency: Bar)(baz: dependency.Baz)` but this approach would not mix well with implicit parameters and also adds a parameter to a lot of methods.

## Dependencies between concerns

Concerns reified as typeclasses. Implicit parameters for injection. It works because a lot of stuff can be type-directed. It requires to be careful not to forget to add an implicit parameter otherwise the implicit resolution can be messed.

## Specialization using named parameters

## Separation of concerns, modularity

Not all presentational information can be derived from a data type definition. You need a mean of adding presentational information. Annotations on the data type definition could help but would lead to mix concerns.

## Typeclasses factory methods

