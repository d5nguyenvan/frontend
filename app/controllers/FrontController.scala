package controllers

import conf._
import com.gu.openplatform.contentapi.model.ItemResponse
import common.{ Configuration => NotWantedHere, _ }
import play.api.mvc.{ Controller, Action }

case class NetworkFrontPage(editorsPicks: Seq[Trail])

object FrontController extends Controller with Logging {

  object FrontMetaData extends MetaData {
    override val canonicalUrl = "http://www.guardian.co.uk"
    override val id = ""
    override val section = ""
    override val apiUrl = "http://content.guardianapis.com"
    override val webTitle = "The Guardian"
  }

  def render() = Action { implicit request =>
    val edition = Configuration.edition(OriginDomain(request))
    lookup(edition) map { renderFront(_) } getOrElse { NotFound }
  }

  private def lookup(edition: String): Option[NetworkFrontPage] = suppressApi404 {

    log.info("Fetching network front")
    val response: ItemResponse = ContentApi.item
      .edition(edition)
      .showTags("all")
      .showFields("all")
      .showMedia("all")
      .showEditorsPicks(true)
      .showMostViewed(true)
      .itemId("")
      .response

    val editorsPicks = response.editorsPicks map { new Content(_) }

    Some(NetworkFrontPage(editorsPicks))
  }

  private def renderFront(model: NetworkFrontPage) = Ok(views.html.front(FrontMetaData, model.editorsPicks))
}