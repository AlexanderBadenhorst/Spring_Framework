package tacos;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HomePageBrowserTest {

    @LocalServerPort
    private int port;

    private static WebDriver browser;

    @BeforeAll
    static void setup() {
        // enable JS for HtmlUnit so modern pages behave
        browser = new HtmlUnitDriver(true);
        browser.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterAll
    static void teardown() {
        if (browser != null) {
            browser.quit();
        }
    }

    @Test
    void testHomePage() {
        String homePage = "http://localhost:" + port + "/"; // trailing slash helps with URL joins
        browser.get(homePage);

        String titleText = browser.getTitle();
        Assertions.assertThat(titleText).isEqualTo("Taco Cloud");

        String h1Text = browser.findElement(By.tagName("h1")).getText();
        Assertions.assertThat(h1Text).isEqualTo("Welcome to...");

        String imgSrc = browser.findElement(By.tagName("img")).getAttribute("src");
        Assertions.assertThat(imgSrc).isEqualTo(homePage + "images/TacoCloud.png");
    }
}
