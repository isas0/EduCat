package test.system;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;

/**
 * SYSTEM TEST implementando i casi specifici del Test Plan (Sezione 9.2)
 * Test Black-Box end-to-end seguendo gli oracoli definiti
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EduCatSystemTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:80/educat";

    @BeforeAll
    static void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void cleanup() {
        try {
            driver.get(BASE_URL + "/logout");
        } catch (Exception e) {
        }
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }

    // ============== TC_LEZ_05: LOGIN ==============

    @Test
    @DisplayName("TC_LEZ_05_01: Login successo (Happy Path) - Studente")
    @Order(1)
    void TC_LEZ_05_01_LoginSuccessoStudente() {
        driver.get("http://localhost/educat/");
        driver.findElement(By.linkText("Accedi qui")).click();
        driver.findElement(By.name("email")).click();
        driver.findElement(By.name("email")).sendKeys("studente@test.it");
        driver.findElement(By.name("password")).click();
        driver.findElement(By.name("password")).sendKeys("password");
        driver.findElement(By.cssSelector(".submit-btn")).click();
        
        // CORREZIONE: Usa assertTrue() di JUnit, NON verify() di Mockito
        assertTrue(!driver.getPageSource().toLowerCase().contains("error="),
            "Login dovrebbe avere successo senza errori");
        
        wait.until(ExpectedConditions.urlContains("homePageStudenteGenitore.jsp"));
        assertTrue(driver.getCurrentUrl().contains("homePageStudenteGenitore.jsp"),
            "Dopo login studente, dovrebbe reindirizzare alla home studente");
    }

    @Test
    @DisplayName("TC_LEZ_05_04: Password non associata a mail")
    @Order(2)
    void TC_LEZ_05_04_LoginPasswordSbagliata() {
    	    driver.get("http://localhost/educat/");
    	    driver.findElement(By.linkText("Accedi qui")).click();
    	    driver.findElement(By.name("email")).click();
    	    driver.findElement(By.name("email")).sendKeys("studente@test.it");
    	    driver.findElement(By.name("password")).click();
    	    driver.findElement(By.name("password")).sendKeys("passwordSbagliata");
    	    driver.findElement(By.cssSelector(".submit-btn")).click();
    	    
    	    assertTrue(driver.getPageSource().toLowerCase().contains("error="),
    	            "Login dovrebbe fallire");
    	        
    	        wait.until(ExpectedConditions.urlContains("login.jsp"));
    	        assertTrue(driver.getCurrentUrl().contains("login.jsp"),
    	            "Dopo login errato, dovrebbe reindirizzare alla pagina del login");

    }

    // ============== TC_LEZ_01: PRENOTAZIONE ==============

    @Test
    @DisplayName("TC_LEZ_01_01: Prenotazione con successo (Happy Path)")
    @Order(3)
    void TC_LEZ_01_01_PrenotazioneSuccesso() throws InterruptedException {
        
        TC_LEZ_05_01_LoginSuccessoStudente();
        
        driver.get(BASE_URL + "/info-lezione?idLezione=24"); 
        
        driver.findElement(By.id("pay")).click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cardNumber")));
        
        driver.findElement(By.id("cardNumber")).sendKeys("1234567812345678");
        driver.findElement(By.id("expiryDate")).sendKeys("12/29");
        driver.findElement(By.id("cvv")).sendKeys("123");
        driver.findElement(By.id("cardName")).sendKeys("MARIO ROSSI");
        
        
        driver.findElement(By.id("btnPay")).click();
        
        
        assertTrue(driver.getPageSource().toLowerCase().contains("prenotazioni.jsp") && !driver.getPageSource().toLowerCase().contains("error"));
    }

    @Test
    @DisplayName("TC_LEZ_01_07: Numero carta vuoto")
    @Order(4)
    void TC_LEZ_01_07_PrenotazioneCartaVuota() {

    	TC_LEZ_05_01_LoginSuccessoStudente();
        
        driver.get(BASE_URL + "/info-lezione?idLezione=24"); 
        
        driver.findElement(By.id("pay")).click();
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("cardNumber")));
        
        driver.findElement(By.id("cardNumber")).sendKeys("");
        driver.findElement(By.id("expiryDate")).sendKeys("12/29");
        driver.findElement(By.id("cvv")).sendKeys("123");
        driver.findElement(By.id("cardName")).sendKeys("MARIO ROSSI");
        
        
        driver.findElement(By.id("btnPay")).click();
        
        //Non ha effettuato la prenotazione
        assertTrue(driver.getPageSource().toLowerCase().contains("checkout.jsp"));
    }

    // ============== TC_LEZ_07: RICERCA LEZIONE ==============

    @Test
    @DisplayName("TC_LEZ_07_01: Ricerca senza filtri (tutte le lezioni)")
    @Order(5)
    void TC_LEZ_07_01_RicercaSenzaFiltri() {
        
        TC_LEZ_05_01_LoginSuccessoStudente();
        driver.findElement(By.cssSelector(".submit-btn")).click();
        driver.findElement(By.cssSelector(".btn-search")).click(); 
        assertTrue(driver.getPageSource().toLowerCase().contains("lista"));
        assertTrue(resultsContainMateria("Matematica"));
    }
   


    @Test
    @DisplayName("TC_LEZ_07_02: Ricerca solo per materia")
    @Order(6)
    public void TC_LEZ_07_02_RicercaSoloMateria() {
    	driver.get("http://localhost/educat/");
    	driver.findElement(By.linkText("Accedi qui")).click();
    	driver.findElement(By.name("email")).click();
    	driver.findElement(By.name("email")).sendKeys("studente@test.it");
    	driver.findElement(By.name("password")).click();
    	driver.findElement(By.name("password")).sendKeys("password");
    	driver.findElement(By.cssSelector(".submit-btn")).click();
    	driver.findElement(By.name("materia")).click();
    	{
    		WebElement dropdown = driver.findElement(By.name("materia"));
    		dropdown.findElement(By.xpath("//option[. = 'Matematica']")).click();
    	}
    	driver.findElement(By.cssSelector(".btn-search")).click();
    	assertTrue(driver.getPageSource().toLowerCase().contains("lista"));
    }

    private boolean resultsContainMateria(String materia) {
        try {
            java.util.List<WebElement> elements = driver.findElements(
                By.xpath("//*[contains(text(), '" + materia + "')]"));
            return !elements.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}