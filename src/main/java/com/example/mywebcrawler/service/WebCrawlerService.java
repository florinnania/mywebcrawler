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
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

/**
 * Service class for web crawling using Jsoup.
 */
@Service
public class WebCrawlerService {

  private static String BASE_URL = "https://tomblomfield.com/";

  private final ObjectMapper objectMapper;
  private final ForkJoinPool forkJoinPool;

  private final Map<String, Set<String>> siteMap;

  /**
   * Constructs a new WebCrawlerService.
   *
   * @param objectMapper ObjectMapper for JSON serialization.
   */
  public WebCrawlerService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.forkJoinPool = new ForkJoinPool();
    this.siteMap = new ConcurrentHashMap<>(); // ConcurrentHashMap for thread-safety
  }

  /**
   * Initiates the web crawling process.
   */
  public void crawl() throws RuntimeException {
    forkJoinPool.invoke(new CrawlTask(BASE_URL));
    forkJoinPool.shutdown();
  }

  /**
   * RecursiveTask to crawl a URL and its child URLs, populating the siteMap.
   */
  private class CrawlTask extends RecursiveTask<Void> {
    private final String url;

    public CrawlTask(String url) {
      this.url = url;
    }

    @Override
    protected Void compute() {
      if (!siteMap.containsKey(url)) {
        Set<String> links;
        try {
          links = extractLinks(url);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        siteMap.put(url, links);

        Set<CrawlTask> tasks = new HashSet<>();
        for (String link : links) {
          tasks.add(new CrawlTask(link));
        }

        invokeAll(tasks);
      }
      return null;
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
