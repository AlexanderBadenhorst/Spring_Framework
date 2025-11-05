package tacos;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class DesignAndOrderTacosBrowserTest {

    private static WebDriver browser;

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate rest;

    @BeforeAll
    static void setup() {
        // HtmlUnitDriver with JS enabled for modern pages
        browser = new HtmlUnitDriver(true);
        browser.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.quit();
        }
    }

    @Test
    void testDesignATacoPage_HappyPath() {
        browser.get(homePageUrl());
        clickDesignATaco();
        assertDesignPageElements();
        buildAndSubmitATaco("Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
        clickBuildAnotherTaco();
        buildAndSubmitATaco("Another Taco", "COTO", "CARN", "JACK", "LETC", "SRCR");
        fillInAndSubmitOrderForm();
        assertThat(browser.getCurrentUrl()).isEqualTo(homePageUrl());
    }

    @Test
    void testDesignATacoPage_EmptyOrderInfo() {
        browser.get(homePageUrl());
        clickDesignATaco();
        assertDesignPageElements();
        buildAndSubmitATaco("Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
        submitEmptyOrderForm();
        fillInAndSubmitOrderForm();
        assertThat(browser.getCurrentUrl()).isEqualTo(homePageUrl());
    }

    @Test
    void testDesignATacoPage_InvalidOrderInfo() {
        browser.get(homePageUrl());
        clickDesignATaco();
        assertDesignPageElements();
        buildAndSubmitATaco("Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
        submitInvalidOrderForm();
        fillInAndSubmitOrderForm();
        assertThat(browser.getCurrentUrl()).isEqualTo(homePageUrl());
    }

    // ----- Actions & assertions -----

    private void buildAndSubmitATaco(String name, String... ingredients) {
        assertDesignPageElements();

        for (String ingredient : ingredients) {
            browser.findElement(By.cssSelector("input[value='" + ingredient + "']")).click();
        }
        browser.findElement(By.cssSelector("input#name")).sendKeys(name);
        browser.findElement(By.cssSelector("form")).submit();
    }

    private void assertDesignPageElements() {
        assertThat(browser.getCurrentUrl()).isEqualTo(designPageUrl());

        List<WebElement> ingredientGroups = browser.findElements(By.className("ingredient-group"));
        assertThat(ingredientGroups.size()).isEqualTo(5);

        WebElement wrapGroup = browser.findElement(By.cssSelector("div.ingredient-group#wraps"));
        List<WebElement> wraps = wrapGroup.findElements(By.tagName("div"));
        assertThat(wraps.size()).isEqualTo(2);
        assertIngredient(wrapGroup, 0, "FLTO", "Flour Tortilla");
        assertIngredient(wrapGroup, 1, "COTO", "Corn Tortilla");

        WebElement proteinGroup = browser.findElement(By.cssSelector("div.ingredient-group#proteins"));
        List<WebElement> proteins = proteinGroup.findElements(By.tagName("div"));
        assertThat(proteins.size()).isEqualTo(2);
        assertIngredient(proteinGroup, 0, "GRBF", "Ground Beef");
        assertIngredient(proteinGroup, 1, "CARN", "Carnitas");

        WebElement cheeseGroup = browser.findElement(By.cssSelector("div.ingredient-group#cheeses"));
        List<WebElement> cheeses = cheeseGroup.findElements(By.tagName("div"));
        assertThat(cheeses.size()).isEqualTo(2);
        assertIngredient(cheeseGroup, 0, "CHED", "Cheddar");
        assertIngredient(cheeseGroup, 1, "JACK", "Monterrey Jack");

        WebElement veggieGroup = browser.findElement(By.cssSelector("div.ingredient-group#veggies"));
        List<WebElement> veggies = veggieGroup.findElements(By.tagName("div"));
        assertThat(veggies.size()).isEqualTo(2);
        assertIngredient(veggieGroup, 0, "TMTO", "Diced Tomatoes");
        assertIngredient(veggieGroup, 1, "LETC", "Lettuce");

        WebElement sauceGroup = browser.findElement(By.cssSelector("div.ingredient-group#sauces"));
        List<WebElement> sauces = sauceGroup.findElements(By.tagName("div"));
        assertThat(sauces.size()).isEqualTo(2);
        assertIngredient(sauceGroup, 0, "SLSA", "Salsa");
        assertIngredient(sauceGroup, 1, "SRCR", "Sour Cream");
    }

    private void fillInAndSubmitOrderForm() {
        assertThat(browser.getCurrentUrl()).startsWith(orderDetailsPageUrl());
        fillField("input#deliveryName", "Ima Hungry");
        fillField("input#deliveryStreet", "1234 Culinary Blvd.");
        fillField("input#deliveryCity", "Foodsville");
        fillField("input#deliveryState", "CO");
        fillField("input#deliveryZip", "81019");
        fillField("input#ccNumber", "4111111111111111");
        // Use a future date so validation passes
        fillField("input#ccExpiration", "12/30");
        fillField("input#ccCVV", "123");
        browser.findElement(By.cssSelector("form")).submit();
    }

    private void submitEmptyOrderForm() {
        assertThat(browser.getCurrentUrl()).isEqualTo(currentOrderDetailsPageUrl());
        browser.findElement(By.cssSelector("form")).submit();

        assertThat(browser.getCurrentUrl()).isEqualTo(orderDetailsPageUrl());

        List<String> validationErrors = getValidationErrorTexts();
        assertThat(validationErrors.size()).isEqualTo(9);
        assertThat(validationErrors).containsExactlyInAnyOrder(
                "Please correct the problems below and resubmit.",
                "Delivery name is required",
                "Street is required",
                "City is required",
                "State is required",
                "Zip code is required",
                "Not a valid credit card number",
                "Must be formatted MM/YY",
                "Invalid CVV"
        );
    }

    private List<String> getValidationErrorTexts() {
        return browser.findElements(By.className("validationError"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    private void submitInvalidOrderForm() {
        assertThat(browser.getCurrentUrl()).startsWith(orderDetailsPageUrl());
        fillField("input#deliveryName", "I");
        fillField("input#deliveryStreet", "1");
        fillField("input#deliveryCity", "F");
        fillField("input#deliveryState", "C");
        fillField("input#deliveryZip", "8");
        fillField("input#ccNumber", "1234432112344322");
        fillField("input#ccExpiration", "14/91");
        fillField("input#ccCVV", "1234");
        browser.findElement(By.cssSelector("form")).submit();

        assertThat(browser.getCurrentUrl()).isEqualTo(orderDetailsPageUrl());

        List<String> validationErrors = getValidationErrorTexts();
        assertThat(validationErrors.size()).isEqualTo(4);
        assertThat(validationErrors).containsExactlyInAnyOrder(
                "Please correct the problems below and resubmit.",
                "Not a valid credit card number",
                "Must be formatted MM/YY",
                "Invalid CVV"
        );
    }

    private void fillField(String cssSelector, String value) {
        WebElement field = browser.findElement(By.cssSelector(cssSelector));
        field.clear();
        field.sendKeys(value);
    }

    private void assertIngredient(WebElement ingredientGroup,
                                  int ingredientIdx, String id, String name) {
        List<WebElement> items = ingredientGroup.findElements(By.tagName("div"));
        WebElement ingredient = items.get(ingredientIdx);
        assertThat(ingredient.findElement(By.tagName("input")).getAttribute("value")).isEqualTo(id);
        assertThat(ingredient.findElement(By.tagName("span")).getText()).isEqualTo(name);
    }

    private void clickDesignATaco() {
        assertThat(browser.getCurrentUrl()).isEqualTo(homePageUrl());
        browser.findElement(By.cssSelector("a#design")).click();
    }

    private void clickBuildAnotherTaco() {
        assertThat(browser.getCurrentUrl()).startsWith(orderDetailsPageUrl());
        browser.findElement(By.cssSelector("a#another")).click();
    }

    // ----- URL helpers -----

    private String designPageUrl() {
        return homePageUrl() + "design";
    }

    private String homePageUrl() {
        return "http://localhost:" + port + "/";
    }

    private String orderDetailsPageUrl() {
        return homePageUrl() + "orders";
    }

    private String currentOrderDetailsPageUrl() {
        return homePageUrl() + "orders/current";
    }
}
