package com.qa.nal.utils;

import com.microsoft.playwright.Page;
import com.qa.nal.BaseTest;
import org.junit.jupiter.api.extension.*;
import java.nio.file.*;

public class ScreenshotOnFailureWatcher implements AfterTestExecutionCallback, TestInstancePostProcessor {

    private Object testInstance;

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        this.testInstance = testInstance;
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isPresent()) {
            try {
                if (testInstance instanceof BaseTest) {
                    BaseTest baseTest = (BaseTest) testInstance;
                    Page page = baseTest.getPage();
                    if (page != null) {
                        byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
                        Path path = Paths.get("screenshots", context.getDisplayName() + ".png");
                        Files.createDirectories(path.getParent());
                        Files.write(path, screenshot);
                        System.out.println("Saved failure screenshot: " + path.toAbsolutePath());
                    }
                }
            } catch (Exception e) {
                System.err.println("Screenshot capture failed: " + e.getMessage());
            }
        }
    }
}
