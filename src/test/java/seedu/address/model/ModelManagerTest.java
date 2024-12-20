package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.Person;
import seedu.address.storage.BackupManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains unit tests for {@code ModelManager}.
 */
public class ModelManagerTest {

    @TempDir
    public Path temporaryFolder;
    private ModelManager modelManager;
    private StorageManager storage;
    private UserPrefs userPrefs;
    private Path backupDirectoryPath;

    @BeforeEach
    public void setUp() throws IOException {
        Path addressBookPath = temporaryFolder.resolve("addressBook.json");
        Path userPrefsPath = temporaryFolder.resolve("userPrefs.json");

        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(addressBookPath);
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(userPrefsPath);

        storage = new StorageManager(addressBookStorage, userPrefsStorage);
        userPrefs = new UserPrefs();

        // Initialize backup directory for BackupManager
        backupDirectoryPath = temporaryFolder.resolve("backups");
        Files.createDirectories(backupDirectoryPath);
        storage.setBackupManager(new BackupManager(backupDirectoryPath));

        modelManager = new ModelManager(new AddressBook(), userPrefs, storage);
    }

    @AfterEach
    public void cleanUpDefaultBackupDirectory() throws IOException {
        Path defaultBackupDirectory = Paths.get("backups");
        if (Files.exists(defaultBackupDirectory)) {
            Files.walk(defaultBackupDirectory)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete file: " + path + " - " + e.getMessage());
                        }
                    });
        }
    }

    @Test
    public void constructor_initialization_successful() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
        assertNotNull(modelManager.getCalendar());
    }

    @Test
    public void setUserPrefs_updatesUserPreferences() {
        UserPrefs newPrefs = new UserPrefs();
        newPrefs.setAddressBookFilePath(temporaryFolder.resolve("newAddressBook.json"));
        modelManager.setUserPrefs(newPrefs);
        assertEquals(newPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_updatesGuiSettings() {
        GuiSettings newGuiSettings = new GuiSettings(1000, 1000, 10, 10);
        modelManager.setGuiSettings(newGuiSettings);
        assertEquals(newGuiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void setAddressBookFilePath_updatesAddressBookFilePath() {
        Path newFilePath = temporaryFolder.resolve("newAddressBook.json");
        modelManager.setAddressBookFilePath(newFilePath);
        assertEquals(newFilePath, modelManager.getAddressBookFilePath());
    }

    @Test
    public void backupData_nullActionDescription_backupSuccessful() throws CommandException, IOException {
        // Save the address book to ensure the file exists
        storage.saveAddressBook(modelManager.getAddressBook());

        // Perform backup with null action description
        int backupIndex = modelManager.backupData(null);

        // Verify backup index
        assertTrue(backupIndex >= 0 && backupIndex < 10, "Backup index should be between 0 and 9.");
    }

    @Test
    public void backupData_storageNotInitialized_throwsCommandException() throws IOException {
        ModelManager modelWithoutStorage = new ModelManager(new AddressBook(), new UserPrefs(), null);
        CommandException exception = assertThrows(CommandException.class, (
        ) -> modelWithoutStorage.backupData("test"));
        assertEquals("Failed to create backup: Storage is not initialized!", exception.getMessage());
    }

    @Test
    public void restoreBackup_storageNotInitialized_throwsIoException() throws IOException {
        ModelManager modelWithoutStorage = new ModelManager(new AddressBook(), new UserPrefs(), null);
        IOException exception = assertThrows(IOException.class, () -> modelWithoutStorage.restoreBackup(0));
        assertEquals("Storage is not initialized!", exception.getMessage());
    }

    @Test
    public void addPerson_personAddedSuccessfully() {
        Person person = new PersonBuilder().withName("Test Person").build();
        modelManager.addPerson(person);
        assertTrue(modelManager.getAddressBook().getPersonList().contains(person));
    }

    @Test
    public void backupData_descriptionTooLong_throwsCommandException() throws IOException {
        // Create a description longer than the maximum allowed length
        String longDescription = "a".repeat(300); // Exceeds 250 characters

        // Ensure the address book is saved so the source file exists
        storage.saveAddressBook(modelManager.getAddressBook());

        // Attempt to perform backup with long description
        CommandException exception = assertThrows(CommandException.class, () -> {
            modelManager.backupData(longDescription);
        });

        // Check that the exception message is correct
        assertEquals(
                "Failed to create backup:"
                        + " Backup file name exceeds the maximum length of 250 characters."
                        + " Please shorten your description.",
                exception.getMessage()
        );
    }

    @Test
    public void isBackupAvailable_backupDoesNotExist_returnsFalse() {
        // Check for a backup at an index that was not created
        assertFalse(modelManager.isBackupAvailable(0));
    }

    @Test
    public void restoreBackup_backupNotAvailable_throwsIoException() {
        // Attempt to restore a backup that doesn't exist
        int backupIndex = 0;
        assertThrows(IOException.class, () -> modelManager.restoreBackup(backupIndex));
    }

}
