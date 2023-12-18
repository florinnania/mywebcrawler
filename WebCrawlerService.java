package com.example.mywebcrawler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class WebCrawlerService {

  private static final String BASE_URL = "https://tomblomfield.com/";

  private final ObjectMapper objectMapper;
  private final ThreadLocal<WebDriver> threadLocalDriver = ThreadLocal.withInitial(this::getWebDriver);

  public WebCrawlerService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String crawl() throws IOException {
    Map<String, Set<String>> siteMap = new ConcurrentHashMap<>(); // ConcurrentHashMap for thread-safety
    ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    crawlUrl(BASE_URL, siteMap, executorService);

    executorService.shutdown();
    try {
      executorService.awaitTermination(1, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    try {
      String jsonOutput = objectMapper.writeValueAsString(siteMap);
      System.out.println(jsonOutput);
      return jsonOutput;
    } catch (IOException e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
  }

  private void crawlUrl(String url, Map<String, Set<String>> siteMap, ExecutorService executorService) {
    if (siteMap.containsKey(url)) {
      return;
    }

    Set<String> links = new HashSet<>();
    WebDriver driver = threadLocalDriver.get();

    driver.get(url);
    links.addAll(extractLinks(driver));

    siteMap.put(url, links);

    for (String link : links) {
      //crawlUrl(link, siteMap, executorService);
      executorService.submit(() -> crawlUrl(link, siteMap, executorService));
    }
  }

  private WebDriver getWebDriver() {
    ChromeOptions options = new ChromeOptions();
    // Add any additional options if needed
    options.addArguments("--headless=new");
    return new ChromeDriver(options);
  }

  private Set<String> extractLinks(WebDriver driver) {
    Set<String> links = new HashSet<>();
    List<WebElement> elements = driver.findElements(By.tagName("a"));

    for (WebElement element : elements) {
      String absUrl = element.getAttribute("href");
      if (absUrl != null && (isSameDomain(absUrl) || isRelativeLink(absUrl))) {
        links.add(absUrl);
      }
    }

    return links;
  }

  private boolean isSameDomain(String url) {
    return url.startsWith(BASE_URL);
  }

  private boolean isRelativeLink(String url) {
    return url.startsWith("/");
  }
}
