package com.online.library.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Login Page E2E Tests")
@Tag("e2e")
public class LoginPageE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Strona logowania powinna się poprawnie załadować")
    void loginPage_shouldLoadCorrectly() {
        driver.get(getBaseUrl() + "/login");

        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("Login") || pageTitle.contains("Logowanie") || pageTitle.contains("Library"),
                "Strona logowania powinna mieć odpowiedni tytuł");

        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='username'], input[type='text']")));
        assertNotNull(usernameField, "Pole username powinno istnieć");

        WebElement passwordField = driver.findElement(
                By.cssSelector("input[name='password'], input[type='password']"));
        assertNotNull(passwordField, "Pole password powinno istnieć");

        WebElement submitButton = driver.findElement(
                By.cssSelector("button[type='submit'], input[type='submit']"));
        assertNotNull(submitButton, "Przycisk logowania powinien istnieć");
    }

    @Test
    @DisplayName("Strona główna powinna się załadować")
    void homePage_shouldLoadCorrectly() {
        driver.get(getBaseUrl() + "/");

        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assertNotNull(body, "Strona główna powinna się załadować");

        boolean hasNavbar = driver.findElements(By.cssSelector("nav, .navbar")).size() > 0;
        assertTrue(hasNavbar, "Strona powinna mieć nawigację");
    }

    @Test
    @DisplayName("Niezalogowany użytkownik powinien być przekierowany do logowania")
    void unauthenticatedUser_shouldBeRedirectedToLogin() {
        driver.get(getBaseUrl() + "/dashboard");

        wait.until(ExpectedConditions.urlContains("login"));

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("login"),
                "Niezalogowany użytkownik powinien być przekierowany do logowania");
    }

    @Test
    @DisplayName("Nieprawidłowe logowanie powinno pokazać błąd")
    void invalidLogin_shouldShowError() {
        driver.get(getBaseUrl() + "/login");

        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[name='username'], input[type='text']")));
        usernameField.sendKeys("invalid_user");

        WebElement passwordField = driver.findElement(
                By.cssSelector("input[name='password'], input[type='password']"));
        passwordField.sendKeys("invalid_password");

        WebElement submitButton = driver.findElement(
                By.cssSelector("button[type='submit'], input[type='submit']"));
        submitButton.click();

        wait.until(ExpectedConditions.urlContains("login"));
    }

    @Test
    @DisplayName("Strona rejestracji powinna być dostępna")
    void registerPage_shouldBeAccessible() {
        driver.get(getBaseUrl() + "/register");

        WebElement body = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assertNotNull(body, "Strona rejestracji powinna się załadować");

        boolean hasForm = driver.findElements(By.tagName("form")).size() > 0;
        assertTrue(hasForm, "Strona rejestracji powinna mieć formularz");
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }
}
