/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.main;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxRobot;
import org.testfx.service.query.NodeQuery;

import static io.github.vocabhunter.gui.common.GuiConstants.*;
import static io.github.vocabhunter.gui.main.GuiTestConstants.BOOK_1;
import static io.github.vocabhunter.gui.main.GuiTestConstants.BOOK_2;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

public class GuiTestSteps {
    private static final Logger LOG = LoggerFactory.getLogger(GuiTestSteps.class);

    private final FxRobot robot;

    private final GuiTestValidator validator;

    private int stepNo;

    public GuiTestSteps(final FxRobot robot, final GuiTestValidator validator) {
        this.robot = robot;
        this.validator = validator;
    }

    public void part1BasicWalkThrough() {
        step("Open application", () -> {
            verifyThat("#mainBorderPane", isVisible());
        });

        step("Start new session", () -> {
            robot.clickOn("#buttonNew");
            verifyThat("#mainWordPane", isVisible());
            verifyThat("#mainWord", hasText("and"));
        });

        step("Mark word as known", () -> {
            robot.clickOn("#buttonKnown");
            verifyThat("#mainWord", hasText("the"));
        });

        step("Mark word as unknown", () -> {
            robot.clickOn("#buttonUnknown");
            verifyThat("#mainWord", hasText("to"));
        });

        step("Mark word as known with keyboard", () -> {
            robot.type(KeyCode.K);
            verifyThat("#mainWord", hasText("me"));
        });

        step("Mark word as unknown with keyboard", () -> {
            robot.type(KeyCode.X);
            verifyThat("#mainWord", hasText("of"));
        });

        step("Show selection", () -> {
            robot.clickOn("#buttonEditOff");
            verifyThat("#buttonKnown", isInvisible());
        });

        step("Return to edit mode", () -> {
            robot.clickOn("#buttonEditOn");
            verifyThat("#buttonKnown", isVisible());
        });

        step("Export the selection", () -> {
            robot.clickOn("#buttonExport");
            validator.validateExportFile();
        });

        step("Save the session", () -> {
            robot.clickOn("#buttonSave");
            validator.validateSavedSession(BOOK_1);
        });
    }

    public void part2Progress() {
        step("Check progress", () -> {
            robot.clickOn("#tabProgress");
            verifyThat("#labelValueDone", hasText("4 Words"));
        });

        step("Return to analysis", () -> {
            robot.clickOn("#tabAnalysis");
            verifyThat("#mainWord", hasText("me"));
        });

    }

    public void part3StartNewSessionAndFilter() {
        step("Open a new session for a different book", () -> {
            robot.clickOn("#buttonNew");
            verifyThat("#mainWord", hasText("the"));
        });

        step("Define impossible filter", () -> {
            robot.clickOn("#buttonSetupFilters");
            robot.doubleClickOn("#fieldMinimumLetters").write("1000");
            robot.clickOn("#buttonOk");
            verifyThat("#filterErrorDialogue", isVisible());
        });

        step("Close filter error", () -> {
            robot.clickOn("OK");
        });

        step("Define filter", () -> {
            robot.clickOn("#buttonSetupFilters");
            robot.doubleClickOn("#fieldMinimumLetters").write("6");
            robot.doubleClickOn("#fieldMinimumOccurrences").write("4");
            robot.clickOn("#fieldInitialCapital");
            robot.clickOn("#buttonOk");
            verifyThat("#mainWord", hasText("surgeon"));
        });

        step("Add file to filter", () -> {
            robot.clickOn("#buttonSetupFilters");
            robot.clickOn("#buttonAddList");
            robot.clickOn("#buttonAddFilterFile");
            robot.clickOn("#buttonEdit");
            robot.clickOn("#buttonSeen");
            robot.clickOn("#buttonAddFilterFile");
            robot.clickOn("#buttonOk");
            verifyThat("#mainWord", hasText("workhouse"));
        });

        step("Mark filtered word as known", () -> {
            robot.clickOn("#buttonKnown");
            verifyThat("#mainWord", hasText("parish"));
        });

        step("Disable filter", () -> {
            robot.clickOn("#buttonEnableFilters");
            verifyThat("#mainWord", hasText("parish"));
        });
    }

    public void part4ReopenFirstSession() {
        step("Re-open the old session", () -> {
            robot.clickOn("#buttonOpen");
            robot.clickOn("Discard");
            verifyThat("#mainWord", hasText("of"));
        });
    }

    public void part5ErrorHandling() {
        step("Start session from empty file", () -> {
            robot.clickOn("#buttonNew");
            verifyThat("#errorDialogue", isVisible());
        });
        step("Close error dialogue", () -> {
            robot.clickOn("OK");
        });
    }

    public void part6AboutDialogue() {
        step("Open About dialogue", () -> {
            robot.clickOn("#menuHelp");
            robot.clickOn("#menuAbout");
            verifyThat("#aboutDialogue", isVisible());
        });

        step("Open website from About dialogue", () -> {
            robot.clickOn("#linkWebsite");
            validator.validateWebPage(WEBSITE);
        });

        step("Close About dialogue", () -> {
            robot.clickOn("#buttonClose");
        });
    }

    public void part7WebLinks() {
        step("Open website", () -> {
            robot.clickOn("#menuHelp");
            robot.clickOn("#menuWebsite");
            validator.validateWebPage(WEBSITE);
        });

        step("Open VocabHunter How To", () -> {
            robot.clickOn("#menuHelp");
            robot.clickOn("#menuHowTo");
            validator.validateWebPage(WEBPAGE_HELP);
        });

        step("Open Issue Reporting", () -> {
            robot.clickOn("#menuHelp");
            robot.clickOn("#menuIssue");
            validator.validateWebPage(WEBPAGE_ISSUE);
        });
    }

    public void part8Search() {
        step("Open Search", () -> {
            robot.clickOn("#menuWords");
            robot.clickOn("#menuFind");
            verifyThat("#barSearch", isVisible());
        });
        step("Enter search word", () -> {
            robot.doubleClickOn("#fieldSearch").write("try");
            verifyThat("#mainWord", hasText("country"));
            verifyThat("#labelMatches", hasText("1 of 2 matches"));
        });
        step("Select next match", () -> {
            robot.clickOn("#buttonSearchDown");
            verifyThat("#mainWord", hasText("trying"));
            verifyThat("#labelMatches", hasText("2 of 2 matches"));
        });
        step("Select previous match", () -> {
            robot.clickOn("#buttonSearchUp");
            verifyThat("#mainWord", hasText("country"));
            verifyThat("#labelMatches", hasText("1 of 2 matches"));
        });
        step("Seach with no match", () -> {
            robot.doubleClickOn("#fieldSearch").write("bananas");
            verifyThat("#mainWord", hasText("back"));
            verifyThat("#labelMatches", hasText("No matches"));
        });
        step("Close Search", () -> {
            robot.clickOn("#buttonCloseSearch");
            verifyThat("#barSearch", isInvisible());
        });
    }

    public void part9Exit() {
        step("Restart new session", () -> {
            robot.clickOn("#buttonNew");
            verifyThat("#mainWordPane", isVisible());
            verifyThat("#mainWord", hasText("the"));
        });
        step("Make change to session", () -> {
            robot.clickOn("#buttonKnown");
            verifyThat("#mainWord", hasText("a"));
        });
        step("Cancel exit", () -> {
            robot.clickOn("#menuFile");
            robot.clickOn("#menuExit");
            robot.clickOn(lookup("#unsavedChanges", "Cancel"));
            verifyThat("#mainWord", hasText("a"));
        });
        step("Exit with save", () -> {
            robot.clickOn("#menuFile");
            robot.clickOn("#menuExit");
            robot.clickOn(lookup("#unsavedChanges", "Save"));
            validator.validateSavedSession(BOOK_2);
        });
    }

    private void step(final String step, final Runnable runnable) {
        ++stepNo;
        LOG.info("STEP {}: Begin - {}", stepNo, step);
        runnable.run();
        LOG.info("STEP {}:   End - {}", stepNo, step);
    }

    private Node lookup(final String first, final String... queries) {
        NodeQuery nodeQuery = robot.lookup(first);

        for (String query : queries) {
            nodeQuery = nodeQuery.lookup(query);
        }

        return nodeQuery.query();
    }
}
