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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
public class WebCrawlerService {

  private static String BASE_URL = "https://tomblomfield.com/";

  private final ObjectMapper objectMapper;

  public WebCrawlerService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String crawl() throws RuntimeException {
    Map<String, Set<String>> siteMap = new ConcurrentHashMap<>(); // ConcurrentHashMap for thread-safety
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    //ExecutorService executorService= Executors.newSingleThreadExecutor();

    try {
      crawlUrl(BASE_URL, siteMap, executorService).get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }

    executorService.shutdown();
    try {
      executorService.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }

    try {
      String jsonOutput = objectMapper.writeValueAsString(siteMap);
      System.out.println(jsonOutput);
      return jsonOutput;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }

  }

  private Future crawlUrl(String url, Map<String, Set<String>> siteMap, ExecutorService executorService) throws RuntimeException {

    Future future = null;
    if (siteMap.containsKey(url)) {
      return null;
    }

    Set<String> links = new HashSet<>();
    try {
      Document document = Jsoup.connect(url).get();
      links.addAll(extractLinks(document));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    siteMap.put(url, links);

    for (String link : links) {
      future = executorService.submit(() -> crawlUrl(link, siteMap, executorService));
    }
    return future;
  }

  private Set<String> extractLinks(Document document) {
    Set<String> links = new HashSet<>();
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
}

