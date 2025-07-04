package com.qa.nal;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import com.qa.nal.utils.ExcelReader;
import io.github.cdimascio.dotenv.Dotenv;
import io.qase.commons.annotation.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.*;

@ExtendWith(com.qa.nal.utils.ScreenshotOnFailureWatcher.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("My Application - Core User Flows")
public class SearchTests extends BaseTest {

    Dotenv dotenv = Dotenv.load();
    private final String username = dotenv.get("USERNAME_TKE");
    private final String password = dotenv.get("PASSWORD_TKE");
    private final String loginUrl = dotenv.get("TKE_DEV");
    private final String environment = "tke-dev";
    private final String sheetName = "Generic";

    private static final Logger log = LoggerFactory.getLogger(SearchTests.class);
    public static List<Locator> resultList;
    static boolean htmlfound;
    static boolean elementInvisible;

    @Test
    @Order(1)
    @QaseId(1)
    @QaseTitle("Navigate to Login")
    public void navigateToLoginPage() {
        try {
            page.navigate(loginUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));

            page.waitForURL(url -> url.contains("login"), new Page.WaitForURLOptions().setTimeout(45000));
            Assertions.assertTrue(page.url().contains("login"), "Not redirected on login page");
            log.info("Navigating to login page: {}", loginUrl);
        } catch (Exception e) {
            log.info("Navigating to login page: {}", page.url());
            Assertions.fail("Login page not found: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @QaseId(2)
    @QaseTitle("Perform Login")
    public void performLogin() {
        try {
            Locator usernameInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username"));

            if (usernameInput.isVisible()) {
                page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).click();
                log.info("Username field clicked");

                page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).fill(username);
                log.info("Username field filled");

                page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).click();
                log.info("Password field clicked");

                page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).fill(password);
                log.info("Password field filled");

                Locator loginBtn = page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Login").setExact(true));

                Locator signInBtn = page.getByRole(AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Sign In").setExact(true));

                if (loginBtn.isVisible()) {

                    loginBtn.click();
                    log.info("Login button clicked");

                } else if (signInBtn.isVisible()) {

                    signInBtn.click();
                    log.info("Sign in button clicked");

                } else {

                    log.error("Neither Login nor Sign in button is visible");
                    Assertions.fail("Login/Sign in button not found");

                }
            } else {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login with N7MICROSOFT")).click();
                page.waitForTimeout(2000);
                log.info("Login with N7MICROSOFT  button clicked");

                page.waitForSelector(
                        ".loading-screen-wrapper",
                        new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

                page.waitForURL(
                        url -> url.contains("login.microsoftonline"),
                        new Page.WaitForURLOptions().setTimeout(15000));

                page
                        .getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter your email, phone, or"))
                        .click();
                page.waitForTimeout(750);

                page
                        .getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Enter your email, phone, or"))
                        .fill(username);
                page.waitForTimeout(750);

                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next")).click();
                page.waitForTimeout(750);

                page
                        .getByRole(
                                AriaRole.TEXTBOX,
                                new Page.GetByRoleOptions().setName(Pattern.compile("^Enter the password for .*")))
                        .click();
                page.waitForTimeout(750);

                page
                        .getByRole(
                                AriaRole.TEXTBOX,
                                new Page.GetByRoleOptions().setName(Pattern.compile("^Enter the password for .*")))
                        .fill(password);
                page.waitForTimeout(750);

                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in")).click();
                page.waitForTimeout(750);

                page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Don't show this again")).check();
                page.waitForTimeout(750);

                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yes")).click();
                page.waitForTimeout(750);

            }

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            page.waitForURL(url -> url.contains("/app/new-home"));

            Assertions.assertTrue(page.url().contains("new-home"), "Login did not navigate to home");
            log.info("Login successful, navigated to home page");
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            Assertions.fail("Login failed: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @QaseId(3)
    @QaseTitle("Handle Pop-Up")
    public void handleInitialPopup() {
        try {
            if (page.locator(".modal-content").isVisible()) {
                log.info("Modal Pop-Up found");
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancel")).click();

                page.waitForTimeout(2000);
                page.waitForSelector(
                        ".loading-screen-wrapper",
                        new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
                log.info("Cancel button clicked");
            } else {
                log.error("No modal found or not visible");
            }
        } catch (Exception e) {
            log.error("Error handling initial pop-up: {}", e.getMessage());
            Assertions.fail("Error handling initial pop-up: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @QaseId(4)
    @QaseTitle("Navigate to Intelligent Search")
    public void navigateToIntelligentSearch() {
        try {
            page.waitForTimeout(5000);
            Locator searchCard = page
                    .locator("div")
                    .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Intelligent Search$")));
            if (searchCard.isVisible()) {
                page
                        .locator("div")
                        .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Intelligent Search$")))
                        .click();
                page.waitForTimeout(2000);
                log.info("Intelligent Search Card Clicked!");
                page.waitForURL(url -> url.contains("int-answer"));
                Assertions.assertTrue(page.url().contains("int-answer"));
                log.info("Intelligent Search successfully Opened!");
            } else {
                page.locator("body > app > default-layout > div > aside > div.sidebar.collapsed").hover();
                log.info("Sidebar hovered");

                page.waitForTimeout(1500);

                page.locator("a").filter(new Locator.FilterOptions().setHasText("Intelligent Search")).first().click();
                log.info("Intelligent Search clicked");

                page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Intelligent Answer(s) V1")).click();
                log.info("Intelligent Answer Cicked");

                page.waitForTimeout(2000);

                page.locator("a").filter(new Locator.FilterOptions().setHasText("Intelligent Search")).first().click();
                log.info("Intelligent Search clicked again");

                page.waitForURL(url -> url.contains("int-answer-v1"));
                Assertions.assertTrue(
                        page.url().contains("int-answer-v1"),
                        "Intelligent Search page did not load as expected");

                log.info("Intelligent Search page loaded successfully");
            }

        } catch (Exception e) {
            log.error("Navigation to Intelligent Search failed: {}", e.getMessage());
            Assertions.fail("Navigation to Intelligent Search failed: " + e.getMessage());
        }
    }

    static boolean lastQueryFound = false;
    static String lastQuery = "";

    private void searchAQuery(String query) {
        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.locator("#searchTxt").fill(query);
        log.info("Search Field Filled with: {}", query);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
        page.waitForTimeout(2000);
        log.info("Search button Clicked!");
        if (query == lastQuery) {
            lastQueryFound = true;
            log.info("last Query found!");
        }
        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
    }

    static int solutionSize;

    public boolean checkResults() {
        try {
            page.waitForSelector(".serch-result-list .btn-link", new Page.WaitForSelectorOptions().setTimeout(15000));
            log.info("Solutions found");
        } catch (Exception e) {
            log.info("Solution Not found!");
            return false;
        }
        Locator resultArea = page.locator(".serch-result-list");
        page.waitForLoadState(LoadState.NETWORKIDLE);

        Locator singleResult;
        if (!lastQueryFound) {
            singleResult = resultArea
                    .locator(".btn-link")
                    .filter(new Locator.FilterOptions().setHasNotText("open_in_new"))
                    .filter(new Locator.FilterOptions().setHasNotText(".html"));
        } else {
            singleResult = resultArea
                    .locator(".btn-link")
                    .filter(new Locator.FilterOptions().setHasNotText("open_in_new"));
        }

        resultList = singleResult.all();

        solutionSize = resultList.size();
        log.info("Solution List size: {}", solutionSize);

        for (Locator result : resultList) {
            String text = result.textContent().toLowerCase();
            log.info(text);
        }

        return !resultList.isEmpty();
    }

    public void copyLink() {
        try {
            page
                    .getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Copy Link"))
                    .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            log.info("Solutions found for Copy Link");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Copy Link")).click();
        } catch (Exception e) {
            log.error("Copy Link button not found or failed: {}", e.getMessage());
            Assertions.fail("Copy Link button not found or failed: " + e.getMessage());
        }
    }

    public void resultFilter() {
        try {
            // Get all checkboxes inside the expanded filter panel
            List<Locator> checkboxes = page.locator("div.mat-expansion-panel-body mat-checkbox").all();
            log.info("Total filter checkboxes found: " + checkboxes.size());

            boolean clicked = false;

            for (int i = 0; i < checkboxes.size(); i++) {
                Locator checkbox = checkboxes.get(i);

                // Get the wrapper/parent text (label + count)
                Locator wrapper = checkbox.locator("xpath=parent::*");
                String fullText = wrapper.textContent().trim();

                // Extract count from text like "Mazor (4)"
                Pattern pattern = Pattern.compile("\\((\\d+)\\)");
                Matcher matcher = pattern.matcher(fullText);
                int count = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;

                log.info("Filter option: '{}', Result count: {}", fullText, count);

                if (count > 0) {
                    // Click the checkbox inner container
                    Locator matCheckbox = checkbox.locator(".mat-checkbox-inner-container");
                    Locator inputCheckBox = checkbox.locator("input[type=\"checkbox\"]");

                    if (matCheckbox.isVisible()) {
                        matCheckbox.click();
                    } else if (inputCheckBox.isVisible()) {
                        inputCheckBox.click();
                    } else {
                        log.warn("No visible checkbox found for filter: '{}'", fullText);
                        continue;
                    }

                    page.waitForTimeout(2000);

                    checkResults();

                    if (count == solutionSize) {
                        log.info("✅ Results Validated! Filter Result: {}, Visible Results: {}", count, solutionSize);
                    } else {
                        log.warn("❌ Results Mismatched! Filter Result: {}, Visible Results: {}", count, solutionSize);
                    }

                    clicked = true;
                    break;
                } else if (count == 0) {
                    log.info("No results for filter: '{}', skipping...", fullText);
                    continue;
                }
            }

            if (!clicked) {
                log.warn("⚠️ No clickable checkbox with results > 0 was found.");
            }

        } catch (Exception e) {
            log.error("❌ Filter not applied: {}", e.getMessage());
            Assertions.fail("Filter not applied: " + e.getMessage());
        }
    }

    static String queryForSearchInPDF = "";

    public void handleResult() {
        htmlfound = false;
        elementInvisible = false;

        Random rnd = new Random();
        int idx = rnd.nextInt(resultList.size());
        Locator element = resultList.get(idx);
        String text = element.textContent().toLowerCase();

        log.info("Index: {}", idx);
        log.info("Text: {}", text);

        if (text.contains(".html")) {
            htmlfound = true;
            log.info("HTML found");
            return;
        }

        if (/* text.contains(".html") || */text.contains("open_in_new") || !element.isVisible()) {
            // htmlfound = text.contains(".html");
            elementInvisible = !element.isVisible() || text.contains("open_in_new");
            return;
        }

        element.scrollIntoViewIfNeeded();
        element.click();

        page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        page.waitForTimeout(3000);

        Locator resultBody = page.locator("div.modal-content");

        if (resultBody.isVisible()) {
            log.info("Result Body is visible");

            if (text.contains(".pdf")) {
                handlePDFResults();
                log.info("PDF Result Handled");
            }

            page.locator("button[type=\"button\"][aria-label=\"Close\"].close").click();
            page.waitForTimeout(2000);
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

        } else {
            log.warn("Result Body is not visible, giving feedback without opening result!");

            page.waitForTimeout(2000);
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            // ✅ Navigate from .btn-link to its result container (.top-row-container)
            Locator resultContainer = element.locator("xpath=ancestor::div[contains(@class, 'top-row-container')]");
            Locator downAction = resultContainer.locator(".down-action");

            downAction.waitFor(new Locator.WaitForOptions()
                    .setTimeout(5000)
                    .setState(WaitForSelectorState.VISIBLE));
            downAction.click();
            log.info("Down Action Clicked");

            page.waitForTimeout(2000);
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Can you please tell us why"))
                    .fill("Result not opening!");
            log.info("Feedback Text Filled");

            page.locator("div.text-center button.btn-primary")
                    .filter(new Locator.FilterOptions().setHasText("Submit")).click();
            log.info("Feedback Submitted");

            page.waitForTimeout(2000);
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            htmlfound = true;
        }

    }

    public void handlePDFResults() {
        try {
            Random rnd = new Random();

            page.waitForSelector("pdf-viewer >> div");
            String pageText = page.locator(".navigation-pannel span").innerText();
            int totalPages = Integer.parseInt(pageText.split("of")[1].trim());
            int randomPage = rnd.nextInt(totalPages) + 1;
            page.getByPlaceholder("Enter page no").fill("" + randomPage);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Go").setExact(true)).click();
            page.waitForTimeout(2000);
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            page.waitForSelector("pdf-viewer >> div");
            if (randomPage > 1) {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("First")).click();
                page.waitForTimeout(2000);
                page.waitForSelector(
                        ".loading-screen-wrapper",
                        new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
                page.waitForSelector("pdf-viewer >> div");
            }
            if (randomPage < totalPages) {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Last")).click();
                page.waitForTimeout(2000);
                page.waitForSelector(
                        ".loading-screen-wrapper",
                        new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
                page.waitForSelector("pdf-viewer >> div");
            }

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            // page.getByRole(AriaRole.BUTTON, new
            // Page.GetByRoleOptions().setName("Download")).click();

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            Locator searchInPdf = page.locator("div.col-sm-8 input[type=\"text\"]");

            if (searchInPdf.isVisible()) {
                searchInPdf.click();

                searchInPdf.clear();

                searchInPdf.fill(queryForSearchInPDF);
                log.info("Search in PDF filled with 'search text'");

                searchInPdf.press("Enter");
                log.info("Searched Query inside PDF: {}", queryForSearchInPDF);

                page.waitForSelector(
                        ".loading-screen-wrapper",
                        new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

                page.waitForTimeout(2000);
            } else {
                log.warn("Search in PDF input not availabel, skipping search in PDF");
            }

            Locator matAdditionalResult = page.locator("mat-chip-option.mat-mdc-chip");

            if (matAdditionalResult.first().isVisible()) {
                page.waitForTimeout(2000);

                List<Locator> additionalResults = matAdditionalResult.all();
                log.info("Additional results found: {}", additionalResults.size());
                Random random = new Random();
                int randomIndex = random.nextInt(additionalResults.size());
                additionalResults.get(randomIndex).click();
                log.info("Clicking additional result: {}", additionalResults.get(randomIndex).textContent());
                page.waitForTimeout(2000);
                page.waitForSelector(
                        ".loading-screen-wrapper",
                        new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            } else {
                log.info("No additional results found.");
            }
        } catch (Exception e) {
            log.error("Error handling PDF results: {}", e.getMessage());
            Assertions.fail("Error handling PDF results: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @QaseId(5)
    @QaseTitle("Search Query")
    public void testSearchQueries() {
        List<String> searchQueries = ExcelReader.readQueriesFromExcel(
                "src/test/resources/IntelligentSearchQueries.xlsx",
                sheetName);
        Assertions.assertFalse(searchQueries.isEmpty(), "No queries found in Excel file.");

        lastQuery = searchQueries.get(searchQueries.size() - 1);
        log.info("Last Query: " + lastQuery);

        for (int i = 0; i < searchQueries.size(); i++) {
            String query = searchQueries.get(i);
            if (i < searchQueries.size() - 1) {
                try {
                    searchAQuery(query);

                    queryForSearchInPDF = searchQueries.get(i + 1);

                    copyLink();

                    if (!checkResults()) {
                        log.info("No results found for query: {}", query);

                        page.waitForTimeout(2500);

                        boolean isVisible = page.locator("mat-accordion.mat-accordion").isVisible();

                        if (isVisible) {
                            log.info("Filters found!");
                            // filter
                            resultFilter();

                        } else {
                            log.info("Filters are not available!");
                        }
                        page.waitForTimeout(2000);

                        continue;
                    }

                    page.waitForTimeout(2500);

                    boolean isVisible = page.locator("mat-accordion.mat-accordion").isVisible();

                    if (isVisible) {
                        log.info("Filters found!");
                        // filter
                        resultFilter();

                    } else {
                        log.info("Filters are not available!");
                    }

                    int pass = 0;
                    while (pass < 2) {
                        handleResult();
                        if (htmlfound || elementInvisible) {
                            htmlfound = false;
                            elementInvisible = false;
                            pass++;
                            continue;
                        }

                        if (pass == 0 && !htmlfound) {
                            page.getByLabel("Feedback").getByTitle("Negative Feedback").click();
                            page.waitForTimeout(2000);
                            page.waitForSelector(
                                    ".loading-screen-wrapper",
                                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

                            page
                                    .getByRole(
                                            AriaRole.TEXTBOX,
                                            new Page.GetByRoleOptions().setName("Can you please tell us why"))
                                    .fill("Issue in this result!");
                            page
                                    .getByRole(
                                            AriaRole.BUTTON,
                                            new Page.GetByRoleOptions().setName("Submit").setExact(true))
                                    .click();
                            page.waitForTimeout(2000);
                            page.waitForSelector(
                                    ".loading-screen-wrapper",
                                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

                            page.waitForLoadState(LoadState.NETWORKIDLE);
                        } else if (pass == 1 && !htmlfound) {
                            page.getByLabel("Feedback").getByTitle("Positive Feedback").click();
                            page.waitForTimeout(2000);
                            page.waitForSelector(
                                    ".loading-screen-wrapper",
                                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

                            page.waitForLoadState(LoadState.NETWORKIDLE);
                        }
                        pass++;
                    }
                } catch (Exception e) {
                    Assertions.fail("Test failed for query: " + query + " - " + e.getMessage());
                }
            }
        }
    }

    @Test
    @Order(6)
    @QaseId(6)
    @QaseTitle("Test Last Query in sheet")
    public void searchLastQuery() {
        try {
            searchAQuery(lastQuery);
            if (!checkResults())
                return;

            page.getByRole(AriaRole.LISTITEM).locator(".up-action").first().click();
            page.waitForTimeout(2000);
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            page.getByRole(AriaRole.LISTITEM).locator(".down-action").nth(0).click();
            page.waitForTimeout(2000);
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            page
                    .getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Can you please tell us why"))
                    .fill("Issue in this result!");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();
            page.waitForTimeout(2000);
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
        } catch (Exception e) {
            Assertions.fail("Test failed for query: " + lastQuery + "- " + e.getMessage());
        }
    }

    @Test
    @Order(7)
    @QaseId(7)
    @QaseTitle("Navigate to Log Management")
    public void navigateToLogManangement() {
        // Placeholder for log management nav logic
        try {
            page.locator("body > app > default-layout > div > aside > div.sidebar.collapsed").hover();
            log.info("Sidebar hovered");

            page.waitForTimeout(2000);

            page.locator("a").filter(new Locator.FilterOptions().setHasText("Intelligent Search")).first().click();
            log.info("Intelligent Search clicked");

            page.waitForTimeout(1500);

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(" Log Management")).click();
            log.info("Log Management Cicked");

            page.waitForURL(url -> url.contains("intelligent-mgmt"));
            Assertions.assertTrue(
                    page.url().contains("intelligent-mgmt"),
                    "Log Management page did not load as expected");

            log.info("Log Management page loaded successfully");
        } catch (Exception e) {
            log.error("Navigation to Log Management failed: {}", e.getMessage());
            Assertions.fail("Navigation to Log Management failed: " + e.getMessage());
        }
    }

    @Test
    @Order(8)
    @QaseId(8)
    @QaseTitle("Create New KB")
    public void manageLogs() {
        try {
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            page.locator(".action-btn").first().click();

            log.info("Clicked edit icon.");

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            Locator checkBox = page.locator("#createKB");
            if (!checkBox.isChecked()) {
                checkBox.check();
                log.info("Create New KB checked.");
            }

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save")).click();
            page.waitForTimeout(2000);
            log.info("Save clicked.");
        } catch (Exception e) {
            Assertions.fail("Log verification failed: " + e.getMessage());
        }
    }

    @Test
    @Order(9)
    @QaseId(9)
    @QaseTitle("Navigate to Self Service Intelligent Search")
    public void navigateSelfServiceIntelligentSearch() {
        try {
            page.locator("body > app > default-layout > div > aside > div.sidebar.collapsed").hover();
            log.info("Sidebar hovered");

            page.waitForTimeout(2000);

            page
                    .getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(" Self Service Intelligent Search"))
                    .click();
            log.info("Self Service Intelligent Search Clicked");

            page.waitForURL(url -> url.contains("self-serve-kb"));
            Assertions.assertTrue(
                    page.url().contains("self-serve-kb"),
                    "Self Service Intelligent Search page did not load as expected");

            log.info("Self Service Intelligent Search page loaded successfully");

            page.waitForTimeout(2000);
        } catch (Exception e) {
            log.error("Navigation to Self Service Intelligent Search failed: {}", e.getMessage());
            Assertions.fail("Navigation to Self Service Intelligent Search failed: " + e.getMessage());
        }
    }

    @Test
    @Order(10)
    @QaseId(10)
    @QaseTitle("Upload File")
    public void testUploadFile() {
        try {
            Locator fileInput = page.locator("input[type='file']");

            fileInput.waitFor(new Locator.WaitForOptions().setTimeout(2000));

            fileInput.hover();
            log.info("File input hovered");

            page.evaluate("selector => document.querySelector(selector).style.display = 'block'", "input[type='file']");

            Path filePath = Paths.get("C:\\Users\\abhay\\Downloads\\Dummy_Issues_Solutions.pdf");

            fileInput.setInputFiles(filePath.toAbsolutePath());
            page.waitForTimeout(1000);

            try {
                // options for posible modals
                Locator modal = page.locator("div")
                        .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Search MODEL$")))
                        .first();
                Locator manufacturer = page.locator("div")
                        .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Search Manufacturer$")))
                        .nth(1);
                // posible type
                Locator type = page.locator("div")
                        .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Search TYPE$")))
                        .first();

                Locator visibleModal = null;

                // type
                if (type.isVisible()) {
                    type.click();
                    page.waitForTimeout(1000);

                    type.locator("div.ng-input input[type=\"text\"]").fill("OBS");
                    page.waitForTimeout(1000);

                    page.waitForSelector(
                            "//ng-dropdown-panel//div[@role='option']",
                            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));

                    List<Locator> typeList = page.locator("//ng-dropdown-panel//div[@role='option']").all();

                    String selectedType = typeList.get(0).textContent().trim();
                    typeList.get(0).click();
                    log.info("Type option clicked: {}", selectedType);

                    page.waitForTimeout(1000);
                }

                // modal or manufacturer
                if (modal.isVisible()) {
                    visibleModal = modal;
                } else if (manufacturer.isVisible()) {
                    visibleModal = manufacturer;
                }

                if (visibleModal != null) {

                    visibleModal.click();
                    page.waitForTimeout(1000);

                    switch (environment) {
                        case "dev-demo":
                            visibleModal.locator("div.ng-input input[type=\"text\"]").fill("ALINITY");
                            break;

                        case "accuray-dev":
                            visibleModal.locator("div.ng-input input[type=\"text\"]").fill("CYBER");
                            break;

                        case "ni-dev":
                            visibleModal.locator("div.ng-input input[type=\"text\"]").fill("DAQ");
                            break;

                        case "swisslog-dev":
                            visibleModal.locator("div.ng-input input[type=\"text\"]").fill("BLOW");
                            break;

                        case "keysight-dev":
                            visibleModal.locator("div.ng-input input[type=\"text\"]").fill("E444");
                            break;

                        case "terumo-dev":
                            visibleModal.locator("div.ng-input input[type=\"text\"]").fill("REVEOS");
                            break;

                        case "dev6":
                            visibleModal.locator("div.ng-input input[type=\"text\"]").fill("pc cor");
                            break;

                        case "626-dev":
                            visibleModal.locator("div.ng-input input[type=\"text\"]").fill("SYMPH");
                            break;

                        case "ciena-poc":
                            visibleModal.locator("div.ng-input input[type=\"text\"]").fill("BLUE");
                            break;

                        case "crane1-dev":
                            visibleModal.locator("div.ng-input input[type=\"text\"]").fill("Gorb");
                            break;

                        default:
                            visibleModal.locator("div.ng-input input[type=\"text\"]").fill("alinity");
                            break;
                    }
                    page.waitForTimeout(1500);

                    page.waitForSelector(
                            "//ng-dropdown-panel//div[@role='option']",
                            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(15000));

                    List<Locator> manufacturersList = page.locator("//ng-dropdown-panel//div[@role='option']").all();

                    String selectedManufacturer = manufacturersList.get(0).textContent().trim();
                    manufacturersList.get(0).click();
                    log.info("Manufacturer option clicked: {}", selectedManufacturer);
                }
            } catch (Exception e) {
                log.info("Input field not found!");
            }
            page.waitForTimeout(1000);

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Upload")).click();
            log.info("File uploaded successfully!");

            page.waitForTimeout(2000);
        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage());
            Assertions.fail("Upload failed: " + e.getMessage());
        }
    }

    @Test
    @Order(11)
    @QaseId(11)
    @QaseTitle("Navigate to Uploaded KB Management")
    public void navigateUploadedKBManagement() {
        try {
            page.locator("body > app > default-layout > div > aside > div.sidebar.collapsed").hover();
            log.info("Sidebar hovered");

            page.waitForTimeout(2000);

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(" Uploaded KB Management")).click();
            log.info("Uploaded KB Management Clicked");

            page.waitForURL(url -> url.contains("uploaded-kbs"));
            Assertions.assertTrue(
                    page.url().contains("uploaded-kbs"),
                    "Uploaded KB Management page did not load as expected");

            log.info("Uploaded KB Management page loaded successfully");

            page.waitForTimeout(2000);
        } catch (Exception e) {
            log.error("Navigation to Uploaded KB Management failed: {}", e.getMessage());
            Assertions.fail("Navigation to Uploaded KB Management failed: " + e.getMessage());
        }
    }

    public void addNewTag() {
        try {
            page.locator("button[title=\"Add New Tags\"]").click();
            log.info("Clicked Add New Tags button.");
            page.waitForTimeout(1000);

            page.waitForSelector(
                    "input[type=\"text\"].mdc-text-field__input",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE));

            page.locator("input[type=\"text\"].mdc-text-field__input").click();
            log.info("Clicked Tag Input Field.");
            page.waitForTimeout(1000);

            page.locator("input[type=\"text\"].mdc-text-field__input").fill("Dummy Tag");
            log.info("Filled Tag Input Field with 'Dummy Tag'.");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Tag")).click();
            log.info("Clicked Add Tag button.");
            page.waitForTimeout(1000);
        } catch (Exception e) {
            log.info("Failed to add new tag: {}", e.getMessage());
        }
    }

    public void editTag() {
        try {
            page.locator("button[title=\"Update Existing Tags\"]").click();
            log.info("Clicked Update Existing Tags button.");
            page.waitForTimeout(1000);

            page.locator("mat-icon[role='img']").filter(new Locator.FilterOptions().setHasText("edit")).first().click();
            log.info("Clicked Edit icon on Tags.");
            page.waitForTimeout(1000);

            page.locator("input[type=\"text\"].mdc-text-field__input").click();
            log.info("Clicked Tag Input Field for editing.");
            page.waitForTimeout(1000);

            page.locator("input[type=\"text\"].mdc-text-field__input").fill("test tag");
            log.info("Filled Tag Input Field with 'test tag'.");

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update Tag")).click();
            log.info("Clicked Update Tag button.");
            page.waitForTimeout(1000);

            page.locator("button[title=\"Save Changes\"]").click();
            log.info("Clicked Save Changes button.");
            page.waitForTimeout(1000);
        } catch (Exception e) {
            log.info("Failed to edit tag: {}", e.getMessage());
        }
    }

    public void transactionLogs() {
        try {
            page
                    .locator("app-custom-edit-stepper div")
                    .filter(new Locator.FilterOptions().setHasText("Transaction Logs"))
                    .nth(1)
                    .click();
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            log.info("Clicked Transaction Logs.");
            page.waitForTimeout(1000);

            page.locator(".action-btn").first().click();
            log.info("Clicked eye icon on Transaction Logs.");
            page.waitForTimeout(1000);

            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Back")).click();
            log.info("Clicked Back button.");
            page.waitForTimeout(1000);

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            log.info("Transaction Logs loaded successfully.");
        } catch (Exception e) {
            log.info("Failed to access Transaction Logs: {}", e.getMessage());
        }
    }

    @Test
    @Order(12)
    @QaseId(12)
    @QaseTitle("Manage KB")
    public void manageKb() {
        try {
            page.locator(".action-btn").first().click();
            page.waitForTimeout(1000);

            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            log.info("Clicked eye icon.");

            // File Preview
            page
                    .locator("app-custom-edit-stepper div")
                    .filter(new Locator.FilterOptions().setHasText("File Preview"))
                    .nth(1)
                    .click();
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            log.info("Clicked File Preview.");

            page.waitForTimeout(3500);

            // files tag
            page
                    .locator("app-custom-edit-stepper div")
                    .filter(new Locator.FilterOptions().setHasText("File Tags"))
                    .nth(1)
                    .click();
            page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
            log.info("Clicked File Tags.");
            page.waitForTimeout(1000);

            try {
                page.waitForSelector(
                        "button[title=\"Add New Tags\"]",
                        new Page.WaitForSelectorOptions().setTimeout(10000));
                log.info("Add New Tags button is visible.");

                // add tag
                addNewTag();
                log.info("Adding new tag.");
                page.waitForTimeout(1000);

                // save changes
                page.locator("button[title=\"Save Changes\"]").click();
                log.info("Clicked Save Changes button.");
                page.waitForTimeout(1000);

                // add another tag and cancel changes
                addNewTag();
                log.info("Adding another new tag.");
                page.waitForTimeout(1000);

                page.locator("button[title=\"Cancel\"]").click();
                log.info("Clicked Cancel button.");
                page.waitForTimeout(1000);

                // edit tag
                editTag();
                log.info("Editing existing tag.");
                page.waitForTimeout(1000);

                // delete tag
                page.locator("button[title=\"Delete Meta Data\"]").click();
                log.info("Clicked Delete button.");
                page.waitForTimeout(1000);

                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Yes")).click();
                log.info("Clicked Yes button to confirm deletion.");
                page.waitForTimeout(1000);
            } catch (Exception e) {
                log.info("Add New Tags button not found!");
                log.info("No Data found for Tags.");

            }

            // Transaction Logs
            transactionLogs();
            page.waitForTimeout(1000);
        } catch (Exception e) {
            log.error("Failed to manage KB: {}", e.getMessage());
            Assertions.fail("Failed to manage KB: " + e.getMessage());
        }
    }
}