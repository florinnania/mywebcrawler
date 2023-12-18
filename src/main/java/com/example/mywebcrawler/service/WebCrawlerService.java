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

@Service
public class WebCrawlerService {

  private static final String BASE_URL = "https://tomblomfield.com/";

  private final ObjectMapper objectMapper;
  private final ThreadLocal<WebDriver> threadLocalDriver = ThreadLocal.withInitial(this::getWebDriver);

  public WebCrawlerService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String crawl() throws IOException {
    Map<String, Set<String>> siteMap = new HashMap<>();

    crawlUrl(BASE_URL, siteMap);

    try {
      String jsonOutput = objectMapper.writeValueAsString(siteMap);
      System.out.println(jsonOutput);
      return jsonOutput;
    } catch (IOException e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
  }

  private void crawlUrl(String url, Map<String, Set<String>> siteMap) {
    if (siteMap.containsKey(url)) {
      return;
    }

    Set<String> links = new HashSet<>();
    WebDriver driver = threadLocalDriver.get();

    driver.get(url);
    links.addAll(extractLinks(driver));

    siteMap.put(url, links);

    for (String link : links) {
      crawlUrl(link, siteMap);
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
