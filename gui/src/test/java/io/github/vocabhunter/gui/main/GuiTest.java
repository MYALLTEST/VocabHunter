/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.main;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import io.github.vocabhunter.analysis.core.CoreConstants;
import io.github.vocabhunter.analysis.core.GuiTaskHandler;
import io.github.vocabhunter.analysis.core.GuiTaskHandlerForTesting;
import io.github.vocabhunter.analysis.core.VocabHunterException;
import io.github.vocabhunter.analysis.session.EnrichedSessionState;
import io.github.vocabhunter.analysis.session.SessionSerialiser;
import io.github.vocabhunter.analysis.settings.FileListManager;
import io.github.vocabhunter.analysis.settings.FileListManagerImpl;
import io.github.vocabhunter.gui.common.Placement;
import io.github.vocabhunter.gui.dialogues.FileDialogue;
import io.github.vocabhunter.gui.dialogues.FileDialogueFactory;
import io.github.vocabhunter.gui.dialogues.FileDialogueType;
import io.github.vocabhunter.gui.services.EnvironmentManager;
import io.github.vocabhunter.gui.services.PlacementManager;
import io.github.vocabhunter.gui.services.WebPageTool;
import io.github.vocabhunter.gui.settings.SettingsManager;
import io.github.vocabhunter.gui.settings.SettingsManagerImpl;
import io.github.vocabhunter.test.utils.TestFileManager;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.api.FxRobot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.github.vocabhunter.gui.main.GuiTestConstants.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.testfx.api.FxToolkit.registerPrimaryStage;
import static org.testfx.api.FxToolkit.setupApplication;

@RunWith(MockitoJUnitRunner.class)
public class GuiTest extends FxRobot implements GuiTestValidator {

    private TestFileManager manager;

    @Mock
    EnvironmentManager environmentManager;

    @Mock
    PlacementManager placementManager;

    @Mock
    private FileDialogueFactory fileDialogueFactory;

    @Mock
    private FileDialogue fileDialogue;

    @Mock
    private WebPageTool webPageTool;

    @Captor
    private ArgumentCaptor<String> webPageCaptor;

    @BeforeClass
    public static void setupSpec() throws Exception {
        if (Boolean.getBoolean("headless")) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
        }
        registerPrimaryStage();
    }

    @Before
    public void setUp() throws Exception {
        when(environmentManager.useSystemMenuBar()).thenReturn(false);
        when(environmentManager.isExitOptionShown()).thenReturn(true);
        when(placementManager.getMainWindow()).thenReturn(new Placement(WINDOW_WIDTH, WINDOW_HEIGHT));

        manager = new TestFileManager(getClass());

        Path settingsFile = manager.addFile(SettingsManagerImpl.SETTINGS_JSON);
        SettingsManager settingsManager = new SettingsManagerImpl(settingsFile);
        Path fileListManagerFile = manager.addFile(FileListManagerImpl.SETTINGS_JSON);
        FileListManager fileListManager = new FileListManagerImpl(fileListManagerFile);

        CoreGuiModule coreModule = new CoreGuiModule();
        Module testModule = new AbstractModule() {
            @Override
            protected void configure() {
                bind(SettingsManager.class).toInstance(settingsManager);
                bind(FileListManager.class).toInstance(fileListManager);
                bind(FileDialogueFactory.class).toInstance(fileDialogueFactory);
                bind(EnvironmentManager.class).toInstance(environmentManager);
                bind(PlacementManager.class).toInstance(placementManager);
                bind(WebPageTool.class).toInstance(webPageTool);
                bind(GuiTaskHandler.class).to(GuiTaskHandlerForTesting.class);
            }
        };
        VocabHunterGuiExecutable.setModules(coreModule, testModule, new StandardEventSourceModule());

        setupApplication(VocabHunterGuiExecutable.class);
    }

    @Override
    public void setUpFileDialogue(final FileDialogueType type, final String file) {
        setUpFileDialogue(type, getResource(file));
    }

    @Override
    public void setUpFileDialogue(final FileDialogueType type, final Path file) {
        when(fileDialogueFactory.create(eq(type), any(Stage.class))).thenReturn(fileDialogue);
        when(fileDialogue.isFileSelected()).thenReturn(true);
        when(fileDialogue.getSelectedFile()).thenReturn(file);
    }

    @After
    public void tearDown() throws Exception {
        manager.cleanup();
    }

    @Test
    public void testWalkThrough() {
        GuiTestSteps steps = new GuiTestSteps(this, this, manager);

        steps.part1BasicWalkThrough();
        steps.part2Progress();
        steps.part3StartNewSessionAndFilter();
        steps.part4ReopenFirstSession();
        steps.part5ErrorHandling();
        steps.part6AboutDialogue();
        steps.part7WebLinks();
        steps.part8Search();
        steps.part9Exit();
    }

    @Override
    public void validateExportFile(final Path file) {
        String text = readFile(file);
        assertThat("Export file content", text, containsString("the"));
    }

    @Override
    public void validateWebPage(final String page) {
        verify(webPageTool, atLeastOnce()).showWebPage(webPageCaptor.capture());
        assertEquals("Website", page, webPageCaptor.getValue());
    }

    private String readFile(final Path file) {
        try {
            return new String(Files.readAllBytes(file), CoreConstants.CHARSET);
        } catch (final IOException e) {
            throw new VocabHunterException(String.format("Unable to read file %s", file), e);
        }
    }

    @Override
    public void validateSavedSession(final Path file, final String name) {
        EnrichedSessionState state = SessionSerialiser.read(file);

        assertEquals("Session state name", name, state.getState().getName());
    }

    private Path getResource(final String file) {
        try {
            URL url = getClass().getResource("/" + file);

            if (url == null) {
                throw new VocabHunterException(String.format("Unable to load %s", file));
            } else {
                    return Paths.get(url.toURI());
            }
        } catch (URISyntaxException e) {
            throw new VocabHunterException("Unable to load file " + file, e);
        }
    }
}
