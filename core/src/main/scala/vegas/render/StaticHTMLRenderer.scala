package vegas.render

import vegas.DSL.{ExtendedUnitSpecBuilder, SpecBuilder}
import vegas.spec.Spec.ExtendedUnitSpec

/**
  * @author Aish Fenton.
  */
case class StaticHTMLRenderer(specJson: String) extends BaseHTMLRenderer {

  def importsHTML(additionalImports: String*) = (JSImports ++ additionalImports).map { s =>
    "<script src=\"" + s + "\" charset=\"utf-8\"></script>"
  }.mkString("\n")

  def headerHTML(additionalImports: String*) =
    s"""
       |<html>
       |  <head>
       |    ${ importsHTML(additionalImports:_*) }
       |  </head>
       |  <body>
    """.stripMargin

  def plotHTML(name: String = this.defaultName) =
    s"""
       | <script>
       |   var embedSpec = {
       |     mode: "vega-lite",
       |     spec: $specJson
       |   }
       |   vg.embed("#$name", embedSpec, function(error, result) {});
       | </script>
       | <div id='$name'></div>
    """.stripMargin

  val footerHTML =
    """
      |  </body>
      |</html>
    """.stripMargin

  def pageHTML(name: String = defaultName) = {
    headerHTML().trim + plotHTML(name) + footerHTML.trim
  }

  /**
    * Typically you'll want to use this method to render your chart. Returns a full page of HTML wrapped in an iFrame
    * for embedding within existing HTML pages (such as Jupyter).
    * XXX Also contains an ugly hack to resize iFrame height to fit chart, if anyone knows a better way open to suggestions
    * @param name The name of the chart to use as an HTML id. Defaults to a UUID.
    * @return HTML containing iFrame for embedding
    */
  def frameHTML(name: String = defaultName) = {
    val frameName = "frame-" + name
    s"""
      |  <iframe id="${frameName}" sandbox="allow-scripts allow-same-origin" style="border: none; width: 100%" srcdoc="${xml.Utility.escape(pageHTML(name))}"></iframe>
      |  <script>
      |    if (typeof resizeIFrame != 'function') {
      |      function resizeIFrame(el, k) {
      |        $$(el.contentWindow.document).ready(function() {
      |          el.style.height = el.contentWindow.document.body.scrollHeight + 'px';
      |        });
      |        if (k <= 10) { setTimeout(function() { resizeIFrame(el, k+1) }, 1000 + (k * 250)) };
      |      }
      |    }
      |    $$().ready( function() { resizeIFrame($$('#${frameName}').get(0), 1); });
      |  </script>
    """.stripMargin
    }

  // Continence method
  def show(implicit fn: String => Unit) = fn(frameHTML())

}

object StaticHTMLRenderer {

  implicit def toStaticHTMLRenderer(sb: SpecBuilder): StaticHTMLRenderer = { new StaticHTMLRenderer(sb.toJson) }

}