package com.example.mywebcrawler.service;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebCrawlerServiceTest {

  @InjectMocks
  private WebCrawlerService webCrawlerService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    ReflectionTestUtils.setField(webCrawlerService, "BASE_URL", "https://tomblomfield.com/");
  }

  @Test
  public void testIsSameDomain() {
    assertTrue(webCrawlerService.isSameDomain("https://tomblomfield.com/page"));
    assertFalse(webCrawlerService.isSameDomain("https://example.com/page"));
  }

  @Test
  public void testIsRelativeLink() {
    assertTrue(webCrawlerService.isRelativeLink("/page"));
    assertFalse(webCrawlerService.isRelativeLink("https://example.com/page"));
  }
}

