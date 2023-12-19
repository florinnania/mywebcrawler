### **Web Crawler project**

Java [Spring Boot](https://spring.io/projects/spring-boot) application which use [jsoup](https://jsoup.org/) to crawls a given website and return a json site map with the following information:
- All the web pages accessible from the base url recursively
- For each web page the links that it has to other web pages

In order to simplify the problem and to not crawl by accident the whole internet we added the condition that the links that should be followed should be under the domain of the given website or [relative links](https://www.coffeecup.com/help/articles/absolute-vs-relative-pathslinks/).

### Running the application
```
docker-compose up
```
This will build the jar and run it using docker-compose.

Visit http://localhost:8080/crawler/crawl in your browser to initiate the crawling process. The site map information will be returned in browser as response of API call and also printed to the console.


### TODO:

- add website to crawl as base64encoded url in query param to be able to crawl any website not only https://tomblomfield.com/
- implement a caching mechanism to not crawl same website on every request
- better error handling on API
- add logging, metrics, external config/secrets etc.
- add CI checks (coverage, checkstyle etc)
- replace jsoup with a performant library for dynamic scraping 