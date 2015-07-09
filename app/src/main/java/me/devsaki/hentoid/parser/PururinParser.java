package me.devsaki.hentoid.parser;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.devsaki.hentoid.database.domains.Attribute;
import me.devsaki.hentoid.database.domains.Content;
import me.devsaki.hentoid.database.enums.AttributeType;
import me.devsaki.hentoid.database.enums.Site;
import me.devsaki.hentoid.database.enums.StatusContent;
import me.devsaki.hentoid.pururin.PururinDto;

/**
 * Created by neko on 20/06/2015.
 */
public class PururinParser {

    private final static String TAG = PururinParser.class.getName();

    public static Content parseContent(String html) {
        Content result = null;
        Document doc = Jsoup.parse(html);
        Elements info = doc.select(".gallery-info-block");
        if (info.size() > 0) {
            result = new Content();
            result.setCoverImageUrl(Site.PURURIN.getUrl() + info.select(".gallery-cover").select("img").attr("src"));
            Element title = doc.select(".otitle").first();
            result.setUrl(doc.select("[itemprop=breadcrumb]").select("a").last().attr("href").replace("/gallery", ""));
            result.setTitle(title.text());

            Elements rows = doc.select(".table-info").select("tr");

            result.setAttributes(new HashMap<AttributeType, List<Attribute>>());

            for (Element element : rows) {
                Element td = element.select("td").first();
                if (td.html().startsWith("Artist")) {
                    result.getAttributes().put(AttributeType.ARTIST, parseAttributes(element, AttributeType.ARTIST));
                } else if (td.html().startsWith("Circle")) {
                    result.getAttributes().put(AttributeType.CIRCLE, parseAttributes(element, AttributeType.CIRCLE));
                } else if (td.html().startsWith("Parod")) {
                    result.getAttributes().put(AttributeType.SERIE, parseAttributes(element, AttributeType.SERIE));
                } else if (td.html().startsWith("Character")) {
                    result.getAttributes().put(AttributeType.CHARACTER, parseAttributes(element, AttributeType.CHARACTER));
                } else if (td.html().startsWith("Content")) {
                    result.getAttributes().put(AttributeType.TAG, parseAttributes(element, AttributeType.TAG));
                } else if (td.html().startsWith("Language")) {
                    result.getAttributes().put(AttributeType.LANGUAGE, parseAttributes(element, AttributeType.LANGUAGE));
                } else if (td.html().startsWith("Scanlator")) {
                    result.getAttributes().put(AttributeType.TRANSLATOR, parseAttributes(element, AttributeType.TRANSLATOR));
                } else if (td.html().startsWith("Categor")) {
                    result.getAttributes().put(AttributeType.CATEGORY, parseAttributes(element, AttributeType.CATEGORY));
                } else if (td.html().startsWith("Uploader")) {
                    List<Attribute> attributes = new ArrayList<>(1);
                    Attribute attribute = new Attribute();
                    Element aUser = element.select("a").first();
                    attribute.setName(aUser.text());
                    attribute.setUrl(aUser.attr("href"));
                    attribute.setType(AttributeType.UPLOADER);
                    attributes.add(attribute);
                    result.getAttributes().put(AttributeType.UPLOADER, attributes);
                } else if (td.html().startsWith("Pages")) {
                    Element tdPage = element.select("td").last();
                    Integer pages = Integer.parseInt(tdPage.html().split(" ")[0]);
                    result.setQtyPages(pages);
                }
            }
            //Description
            result.setHtmlDescription(info.select(".gallery-description").html());

            result.setStatus(StatusContent.SAVED);
            //IsDownloadable
            result.setDownloadable(true);
            result.setSite(Site.PURURIN);
        }
        return result;
    }

    private static List<Attribute> parseAttributes(Element element, AttributeType attributeType) {
        Elements elements = element.select("li");

        List<Attribute> attributes = new ArrayList<>(elements.size());
        for (Element li : elements) {
            Attribute attribute = new Attribute();
            attribute.setType(attributeType);
            attribute.setUrl(li.select("a").attr("href"));
            attribute.setName(li.select("a").text());
            attributes.add(attribute);
        }
        return attributes;
    }

    public static List<String> parseImageList(String html) {
        List<String> imagesUrl = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements aUrls = doc.select(".thumblist").select("a");
        for (Element a : aUrls) {
            imagesUrl.add(a.attr("href"));
        }
        return imagesUrl;
    }

    public static PururinDto catchPururinDto(String html) {
        PururinDto result = null;
        String f = "Pururin.ImageViewer2.init(";
        int index = html.indexOf(f) + f.length();
        int lastIndex = html.indexOf(")", index);
        String json = html.substring(index, lastIndex);
        result = new Gson().fromJson(json, PururinDto.class);

        return result;
    }
}
