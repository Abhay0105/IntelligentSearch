package com.qa.nal;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import com.qa.nal.utils.ExcelReader;
import io.github.cdimascio.dotenv.Dotenv;
import io.qase.commons.annotation.*;
import java.nio.file.*;
import java.nio.file.Paths;
import java.util.*;
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
    private final String username = dotenv.get("APP_USERNAME");
    private final String password = dotenv.get("PASSWORD");
    private final String loginUrl = dotenv.get("DEVDEMO_URL");
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
            try {
                page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).click();
                page.waitForTimeout(2000);
                log.info("Username field clicked");
                page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username")).fill(username);
                log.info("Username field filled");
                page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).click();
                page.waitForTimeout(2000);
                log.info("Password field clicked");
                page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password")).fill(password);
                log.info("Password field filled");
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login").setExact(true)).click();
                page.waitForTimeout(2000);
                log.info("Login button clicked");
            } catch (Exception e) {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login with N7MICROSOFT")).click();
                page.waitForTimeout(2000);
                log.info("Login with N7MICROSOFT  button clicked");
            }
            page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
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
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
                );
                log.info("Cancel button clicked");
            }
        } catch (Exception e) {
            log.error("No modal found or not visible");
        }
    }

    @Test
    @Order(4)
    @QaseId(4)
    @QaseTitle("Navigate to Intelligent Search")
    public void navigateToIntelligentSearch() {
        page
            .locator("div")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^Intelligent Search$")))
            .click();
        page.waitForTimeout(2000);
        log.info("Intelligent Search Card Clicked!");
        page.waitForURL(url -> url.contains("int-answer"));
        Assertions.assertTrue(page.url().contains("int-answer"));
        log.info("Intelligent Search successfully Opened!");
    }

    static boolean cpfFound = false;

    private void searchAQuery(String query) {
        page.waitForSelector(
            ".loading-screen-wrapper",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
        );
        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.locator("#searchTxt").fill(query);
        log.info("Search Field Filled with: {}", query);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
        page.waitForTimeout(2000);
        log.info("Search button Clicked!");
        if (query == "Circuit Pack Failed") {
            cpfFound = true;
            log.info("CPF found!");
        }
        page.waitForSelector(
            ".loading-screen-wrapper",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
        );
    }

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
        if (!cpfFound) {
            singleResult = resultArea
                .locator(".btn-link")
                .filter(new Locator.FilterOptions().setHasNotText("open_in_new"))/*
             * .filter(new
             * Locator.FilterOptions().
             * setHasNotText(".html"))
             */;
        } else {
            singleResult = resultArea
                .locator(".btn-link")
                .filter(new Locator.FilterOptions().setHasNotText("open_in_new"));
        }

        resultList = singleResult.all();

        log.info("Solution List size: {}", resultList.size());

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
            // page.waitForTimeout(2000);
            // log.info("Copy Link button clicked!");

            // Page page2 = context.newPage();

            // page2.onDialog(Dialog::accept);

            // String copiedUrl = (String) page2.evaluate("() =>
            // document.querySelector('button.btn-primary').getAttribute('data-clipboard-text')");
            // page2.navigate(copiedUrl, new
            // Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD));
            // Assertions.assertTrue(
            // page2.url().contains(copiedUrl),
            // "Copied link did not navigate to Intelligent Search");
        } catch (Exception e) {
            log.error("Copy Link button not found or failed: {}", e.getMessage());
            Assertions.fail("Copy Link button not found or failed: " + e.getMessage());
        }
    }

    public void resultFilter() {
        try {
            page.locator("input[type=\"checkbox\"].mdc-checkbox__native-control").first().click();
            log.info("Filter applied to results");
            page.waitForTimeout(2000);
        } catch (Exception e) {
            log.error("Filter not applied: {}", e.getMessage());
            Assertions.fail("Filter not applied: " + e.getMessage());
        }
    }

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
        }

        if (/* text.contains(".html") || */text.contains("open_in_new") || !element.isVisible()) {
            // htmlfound = text.contains(".html");
            elementInvisible = !element.isVisible() || text.contains("open_in_new");
            return;
        }

        element.scrollIntoViewIfNeeded();
        element.click();
        page.waitForTimeout(2000);
        page.waitForSelector(
            ".loading-screen-wrapper",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
        );
        page.waitForSelector("body");

        if (text.contains(".pdf")) {
            page.waitForSelector("pdf-viewer >> div");
            String pageText = page.locator(".navigation-pannel span").innerText();
            int totalPages = Integer.parseInt(pageText.split("of")[1].trim());
            int randomPage = rnd.nextInt(totalPages) + 1;
            page.getByPlaceholder("Enter page no").fill("" + randomPage);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Go").setExact(true)).click();
            page.waitForTimeout(2000);
            page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
            page.waitForSelector("pdf-viewer >> div");
            if (randomPage > 1) {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("First")).click();
                page.waitForTimeout(2000);
                page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
                );
                page.waitForSelector("pdf-viewer >> div");
            }
            if (randomPage < totalPages) {
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Last")).click();
                page.waitForTimeout(2000);
                page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
                );
                page.waitForSelector("pdf-viewer >> div");
            }

            page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );

            // page.getByRole(AriaRole.BUTTON, new
            // Page.GetByRoleOptions().setName("Download")).click();

            page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );

            try {
                page.waitForSelector(
                    "mat-chip-option.mat-mdc-chip",
                    new Page.WaitForSelectorOptions().setTimeout(10000)
                );

                List<Locator> additionalRedsults = page.locator("mat-chip-option.mat-mdc-chip").all();
                log.info("Additional results found: {}", additionalRedsults.size());
                Random random = new Random();
                int randomIndex = random.nextInt(additionalRedsults.size());
                additionalRedsults.get(randomIndex).click();
                log.info("Clicking additional result: {}", additionalRedsults.get(randomIndex).textContent());
                page.waitForTimeout(2000);
                page.waitForSelector(
                    ".loading-screen-wrapper",
                    new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
                );
            } catch (Exception e) {
                log.info("No additional results found.");
            }
        }

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close")).click();
        page.waitForTimeout(2000);
        page.waitForSelector(
            ".loading-screen-wrapper",
            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
        );
    }

    @Test
    @Order(5)
    @QaseId(5)
    @QaseTitle("Search Query")
    public void testSearchQueries() {
        List<String> searchQueries = ExcelReader.readQueriesFromExcel(
            "src/test/resources/IntelligentSearchQueries.xlsx",
            "Devdemo"
        );
        Assertions.assertFalse(searchQueries.isEmpty(), "No queries found in Excel file.");

        for (String query : searchQueries) {
            try {
                searchAQuery(query);

                copyLink();

                // resultFilter();

                if (!checkResults()) {
                    log.info("No results found for query: {}", query);
                    continue;
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
                            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
                        );

                        page
                            .getByRole(
                                AriaRole.TEXTBOX,
                                new Page.GetByRoleOptions().setName("Can you please tell us why")
                            )
                            .fill("Issue in this result!");
                        page
                            .getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit").setExact(true))
                            .click();
                        page.waitForTimeout(2000);
                        page.waitForSelector(
                            ".loading-screen-wrapper",
                            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
                        );

                        page.waitForLoadState(LoadState.NETWORKIDLE);
                    } else if (pass == 1 && !htmlfound) {
                        page.getByLabel("Feedback").getByTitle("Positive Feedback").click();
                        page.waitForTimeout(2000);
                        page.waitForSelector(
                            ".loading-screen-wrapper",
                            new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
                        );

                        page.waitForLoadState(LoadState.NETWORKIDLE);
                    }
                    pass++;
                }
            } catch (Exception e) {
                Assertions.fail("Test failed for query: " + query + " - " + e.getMessage());
            }
        }
    }

    @Test
    @Order(6)
    @QaseId(6)
    @QaseTitle("Test Circuit Pack Failed")
    public void searchCPFQuery() {
        try {
            searchAQuery("Circuit Pack Failed");
            if (!checkResults()) return;

            page.getByRole(AriaRole.LISTITEM).locator(".up-action").first().click();
            page.waitForTimeout(2000);
            page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
            page.getByRole(AriaRole.LISTITEM).locator(".down-action").nth(0).click();
            page.waitForTimeout(2000);
            page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
            page
                .getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Can you please tell us why"))
                .fill("Issue in this result!");
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).click();
            page.waitForTimeout(2000);
            page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
        } catch (Exception e) {
            Assertions.fail("Test failed for query: Circuit Pack Failed - " + e.getMessage());
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

            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(" Log Management")).click();
            log.info("Log Management Cicked");

            page.waitForURL(url -> url.contains("intelligent-mgmt"));
            Assertions.assertTrue(
                page.url().contains("intelligent-mgmt"),
                "Log Management page did not load as expected"
            );

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
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
            page.locator(".action-btn").first().click();

            log.info("Clicked edit icon.");

            page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );

            page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
            Locator checkBox = page.locator("#createKB");
            if (!checkBox.isChecked()) {
                checkBox.check();
                log.info("Create New KB checked.");
            }

            page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
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
                "Self Service Intelligent Search page did not load as expected"
            );

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
    @QaseTitle("Uploaded File")
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

            page.getByRole(AriaRole.COMBOBOX).click();
            page.waitForTimeout(1000);

            page.getByRole(AriaRole.COMBOBOX).fill("alinity");
            page.waitForTimeout(1000);

            page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("ALINITY I")).click();
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
                "Uploaded KB Management page did not load as expected"
            );

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
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE)
            );

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
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
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
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
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
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
            log.info("Clicked eye icon.");

            // File Preview
            page
                .locator("app-custom-edit-stepper div")
                .filter(new Locator.FilterOptions().setHasText("File Preview"))
                .nth(1)
                .click();
            page.waitForSelector(
                ".loading-screen-wrapper",
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
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
                new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN)
            );
            log.info("Clicked File Tags.");
            page.waitForTimeout(1000);

            try {
                page.waitForSelector(
                    "button[title=\"Add New Tags\"]",
                    new Page.WaitForSelectorOptions().setTimeout(10000)
                );
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
// Abhay Bhati