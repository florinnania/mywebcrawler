package com.example.mywebcrawler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Service class for web crawling using Jsoup.
 */
@Service
public class WebCrawlerService {

  private static String BASE_URL = "https://tomblomfield.com/";

  private final ObjectMapper objectMapper;
  private final ExecutorService executorService;

  private final Map<String, Set<String>> siteMap;

  /**
   * Constructs a new WebCrawlerService.
   *
   * @param objectMapper ObjectMapper for JSON serialization.
   */
  public WebCrawlerService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    this.siteMap = new ConcurrentHashMap<>(); // ConcurrentHashMap for thread-safety
  }

  /**
   * Initiates the web crawling process.
   *
   */
  public void crawl() throws RuntimeException {

    crawlUrl(BASE_URL);
    executorService.shutdown();
    try {
      executorService.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Recursively crawls a URL and its child URLs, populating the siteMap.
   *
   * @param url The URL to crawl.
   */
  private void crawlUrl(String url) throws RuntimeException {

    if (siteMap.containsKey(url)) {
      return;
    }

    Set<String> links;
    try {
      links = extractLinks(url);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    siteMap.put(url, links);

    for (String link : links) {
      //crawlUrl(link);
      executorService.submit(() -> crawlUrl(link));
    }
  }

  /**
   * Extracts all hyperlinks from the provided page.
   *
   * @param url The URL to extract links from
   * @return A set of extracted URLs
   */
  private Set<String> extractLinks(String url) throws IOException {
    Set<String> links = new HashSet<>();

    Document document = Jsoup.connect(url).get();

    Elements elements = document.select("a[href]");

    for (Element element : elements) {
      String absUrl = element.absUrl("href");
      if (isSameDomain(absUrl) || isRelativeLink(absUrl)) {
        links.add(absUrl);
      }
    }

    return links;
  }

  boolean isSameDomain(String url) {
    return url.startsWith(BASE_URL);
  }

  boolean isRelativeLink(String url) {
    return url.startsWith("/");
  }

  /**
   * Returns a JSON representation of the siteMap
   *
   * @return JSON representation of the site map.
   */
  public String generateJsonOutput() {
    try {
      String jsonOutput = objectMapper.writeValueAsString(siteMap);
      System.out.println(jsonOutput);
      return jsonOutput;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }
}

