package com.nitrous.iocrawler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.http.Header;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class BasicCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern
            .compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private static final String[] IMG_EXTS = { ".bmp", ".png", ".gif", ".jpg", ".jpeg", ".tiff", ".webp" };

    private boolean isImage(String url) {
        for (String ext : IMG_EXTS) {
            if (url.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * You should implement this function to specify whether the given url should be crawled or not
     * (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(WebURL url) {
        String href = url.getURL().toLowerCase();
        if (isImage(href)) {
            return true;
        }
        return (href.startsWith(BasicCrawlController.BASE_URL) || (href.startsWith("https://developers.google.com/")))
                && !FILTERS.matcher(href).matches();
    }

    private static final long now = System.currentTimeMillis();
    private static final long SECOND = 1000;
    private static final long MINUTE = SECOND * 60;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = HOUR * 24;
    private static final long WEEK = DAY * 7;

    /**
     * This function is called when a page is fetched and ready to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        Date lastModified = getLastModified(page);
        if (lastModified == null) {
            // System.out.println("Unable to parse last-modified header for URL "+url);
            return;
        }

        boolean found = false;
        long time = lastModified.getTime();
        long age = now - time;
        if (age < WEEK) {
            // if (age < DAY) {
            found = search(url, page);
        }
        if (found) {
            // System.out.println("FOUND UPDATED IMAGE: "+url );
        }

        /*
         * int docid = page.getWebURL().getDocid(); String domain = page.getWebURL().getDomain();
         * String path = page.getWebURL().getPath(); String subDomain =
         * page.getWebURL().getSubDomain(); String parentUrl = page.getWebURL().getParentUrl();
         * String anchor = page.getWebURL().getAnchor();
         *
         * System.out.println("Docid: " + docid); System.out.println("Domain: '" + domain + "'");
         * System.out.println("Sub-domain: '" + subDomain + "'"); System.out.println("Path: '" +
         * path + "'"); System.out.println("Parent page: " + parentUrl);
         * System.out.println("Anchor text: " + anchor);
         *
         * if (page.getParseData() instanceof HtmlParseData) { HtmlParseData htmlParseData =
         * (HtmlParseData) page.getParseData(); String text = htmlParseData.getText(); String html =
         * htmlParseData.getHtml(); List<WebURL> links = htmlParseData.getOutgoingUrls();
         *
         * System.out.println("Text length: " + text.length()); System.out.println("Html length: " +
         * html.length()); System.out.println("Number of outgoing links: " + links.size()); }
         *
         * Header[] responseHeaders = page.getFetchResponseHeaders(); if (responseHeaders != null) {
         * System.out.println("Response headers:"); for (Header header : responseHeaders) {
         * System.out.println("\t" + header.getName() + ": " + header.getValue()); } }
         *
         *
         * System.out.println("=============");
         */
    }

    private boolean search(String url, Page page) {
        boolean found = false;
        if (isImage(url.toLowerCase())) {
            found = true;
        } else {
            // System.out.println("Checking new content at: " + url);
            if (page.getParseData() instanceof HtmlParseData) {
                HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                String text = htmlParseData.getText();
                String html = htmlParseData.getHtml();
                int k = text.toLowerCase().indexOf(BasicCrawlController.SEARCH_TERM);
                if (k != -1) {
                    int h = text.length() - k;
                    if (k > 13) {
                        h = 13;
                    }
                    // int h = k - 6;
                    // if( h < 0){
                    // h = 0;
                    // }
                    System.out.println("text Found: " + text.substring(k, k + h));
                    System.out.println("at: " + url);
                    // System.out.println(text.substring(k-6, k + 6));
                    found = true;
                }
                k = html.toLowerCase().indexOf(BasicCrawlController.SEARCH_TERM);
                if (k != -1) {
                    int h = html.length() - k;
                    if (h > 60) {
                        h = 60;
                    }
                    // int h = k - 6;
                    // if( h < 0){
                    // h = 0;
                    // }
                    System.out.println("html Found: " + html.substring(k, k + h));
                    System.out.println("at: " + url);
                    // System.out.println(text.substring(k-6, k + 6));
                    found = true;
                }
            }
        }
        return found;
    }

    private Date getLastModified(Page page) {
        Header h = getHeader(page, "Last-Modified");
        if (h != null) {
            String value = h.getValue();
            try {
                return sdf.parse(value);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    // Last-Modified: Wed, 20 Nov 2013 15:58:24 GMT
    private final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZ";

    private Header getHeader(Page page, String name) {
        for (Header h : page.getFetchResponseHeaders()) {
            if (h.getName().equalsIgnoreCase(name)) {
                return h;
            }
        }
        return null;
    }
}