package com.example.mywebcrawler.rest;

import com.example.mywebcrawler.service.WebCrawlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class WebCrawlerControllerTest {

  @InjectMocks
  private WebCrawlerController webCrawlerController;

  @Mock
  private WebCrawlerService webCrawlerService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testCrawlSuccess() {
    // Mocking
    String expectedJsonOutput = "{\"https://tomblomfield.com/rss\": []}";
    when(webCrawlerService.crawl()).thenReturn(expectedJsonOutput);

    // Execution
    ResponseEntity<String> response = webCrawlerController.crawl();

    // Verification
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(expectedJsonOutput, response.getBody());
  }

  @Test
  public void testCrawlException() {
    // Mocking
    when(webCrawlerService.crawl()).thenThrow(new RuntimeException("Test exception"));

    // Execution
    ResponseEntity<String> response = webCrawlerController.crawl();

    // Verification
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}

