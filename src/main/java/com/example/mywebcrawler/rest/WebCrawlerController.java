package com.example.mywebcrawler.rest;

import com.example.mywebcrawler.service.WebCrawlerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller class for web crawling endpoint.
 */
@RestController
@RequestMapping("/crawler")
public class WebCrawlerController {

  private final WebCrawlerService webCrawlerService;

  public WebCrawlerController(WebCrawlerService webCrawlerService) {
    this.webCrawlerService = webCrawlerService;
  }

  @GetMapping(value = "/crawl", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> crawl() {
    try {
      String jsonOutput = webCrawlerService.crawl();
      return ResponseEntity.ok(jsonOutput);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}

