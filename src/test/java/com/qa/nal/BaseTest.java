package com.qa.nal;

import com.microsoft.playwright.*;

import java.util.Arrays;

import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    protected Playwright pw;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    void initAll() {
        pw = Playwright.create();
        browser = pw.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(50)
                .setArgs(Arrays.asList(
                        "--disable-pdf-material-ui", // For older Chrome builds
                        "--enable-features=NetworkService,NetworkServiceInProcess",
                        "--disable-extensions")));
        context = browser.newContext();
        page = context.newPage();

        // Pass milliseconds as a double, not a Duration
        page.setDefaultTimeout(60000); // 30 seconds

        // auto-accept JS dialogs
        page.onDialog(Dialog::accept);
    }

    public Page getPage() {
        return this.page;
    }

    
    @AfterAll
    void tearDownAll() {
        browser.close();
        pw.close();
    }
}