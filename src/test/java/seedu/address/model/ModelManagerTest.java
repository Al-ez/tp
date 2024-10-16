package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Contains unit tests for {@code ModelManager}.
 */
public class ModelManagerTest {

    @TempDir
    public Path temporaryFolder;
    private ModelManager modelManager;
    private StorageManager storage;
    private UserPrefs userPrefs;

    /**
     * Sets up the test environment with the required storage.
     */
    @BeforeEach
    public void setUp() throws IOException {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));

        storage = new StorageManager(addressBookStorage, userPrefsStorage); // Initialize storage
        userPrefs = new UserPrefs(); // Initialize userPrefs

        modelManager = new ModelManager(new AddressBook(), userPrefs, storage);
    }

    /**
     * Tests whether the constructor initializes the model correctly.
     */
    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void setUserPrefs_nullUserPrefs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setUserPrefs(null));
    }

    @Test
    public void setUserPrefs_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setAddressBookFilePath(Paths.get("address/book/file/path"));
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager.setUserPrefs(userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setAddressBookFilePath(Paths.get("new/address/book/file/path"));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void setAddressBookFilePath_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setAddressBookFilePath(null));
    }

    @Test
    public void setAddressBookFilePath_validPath_setsAddressBookFilePath() {
        Path path = Paths.get("address/book/file/path");
        modelManager.setAddressBookFilePath(path);
        assertEquals(path, modelManager.getAddressBookFilePath());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void equals_sameObject_returnsTrue() {
        assertTrue(modelManager.equals(modelManager), "Comparing the same object should return true.");
    }

    @Test
    public void equals_nullObject_returnsFalse() {
        assertFalse(modelManager.equals(null), "Comparing with null should return false.");
    }

    @Test
    public void equals_differentClass_returnsFalse() {
        assertFalse(modelManager.equals("not a ModelManager"),
                "Comparing with an object of different class should return false.");
    }

    @Test
    public void equals_differentAddressBook_returnsFalse() {
        ModelManager differentModel = new ModelManager(new AddressBook(), userPrefs, storage);
        differentModel.addPerson(ALICE); // Modify to ensure difference
        assertFalse(modelManager.equals(differentModel),
                "Comparing with a different address book should return false.");
    }

    @Test
    public void equals_identicalModelManager_returnsTrue() {
        ModelManager identicalModel = new ModelManager(new AddressBook(), userPrefs, storage);
        assertTrue(modelManager.equals(identicalModel), "Comparing with an identical ModelManager should return true.");
    }


    @Test
    public void backupData_validPath_success() throws Exception {
        Path backupPath = temporaryFolder.resolve("backup.json");
        modelManager.backupData(backupPath.toString());

        // Assert that the file was created successfully.
        assertTrue(backupPath.toFile().exists(), "Backup file should have been created successfully.");
    }

    @Test
    public void modelManager_initialization_validStorage() throws IOException {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(Paths.get("data/addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(Paths.get("data/userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        ModelManager modelManager = new ModelManager(new AddressBook(), new UserPrefs(), storage);
        assertNotNull(modelManager.getStorage(), "Storage should not be null.");
        assertEquals(storage, modelManager.getStorage(), "Storage should be initialized properly.");
    }

    @Test
    public void backupData_withValidStorage_noException() throws IOException {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(Paths.get("data/addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(Paths.get("data/userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        ModelManager modelManager = new ModelManager(new AddressBook(), new UserPrefs(), storage);
        String backupPath = "data/backup.json";

        try {
            modelManager.backupData(backupPath);
        } catch (IOException e) {
            fail("Backup should not throw IOException with valid storage: " + e.getMessage());
        }
    }

    @Test
    public void getStorage_returnsValidStorage() {
        assertNotNull(modelManager.getStorage(), "Storage should not be null.");
        assertEquals(modelManager.getStorage().getClass(),
                StorageManager.class, "Storage should be an instance of StorageManager.");
    }

    /**
     * Tests the constructor that initializes ModelManager with null storage.
     */
    @Test
    public void constructor_nullStorage_success() {
        ModelManager modelManagerWithoutStorage = new ModelManager(new AddressBook(), new UserPrefs(), null);
        assertNotNull(modelManagerWithoutStorage,
                "ModelManager should be created successfully even with null storage.");
    }

    /**
     * Tests the backupData method when storage is null.
     */
    @Test
    public void backupData_nullStorage_throwsIoException() {
        ModelManager modelManagerWithoutStorage = new ModelManager(new AddressBook(), new UserPrefs(), null);
        String backupPath = temporaryFolder.resolve("backup.json").toString();

        assertThrows(IOException.class, () -> modelManagerWithoutStorage.backupData(backupPath),
                "Expected IOException when trying to back up with null storage.");
    }

    @Test
    public void restoreFromBackup_validBackup_returnsTrue() throws IOException {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        ModelManager modelManager = new ModelManager(new AddressBook(), new UserPrefs(), storage);
        String backupPath = temporaryFolder.resolve("backup.json").toString();

        // Backup current state
        modelManager.backupData(backupPath);

        // Modify the address book and restore from backup
        modelManager.addPerson(ALICE);
        boolean restored = modelManager.restoreFromBackup();
        assertTrue(restored, "Backup restoration should be successful.");
        assertFalse(modelManager.hasPerson(ALICE), "ALICE should not exist after restoring the backup.");
    }

    @Test
    public void backupData_withValidStorageAndFilePath_success() throws IOException {
        Path backupPath = temporaryFolder.resolve("backup.json");
        assertDoesNotThrow(() -> modelManager.backupData(backupPath.toString()));
        assertTrue(Files.exists(backupPath), "Backup file should be created successfully.");
    }

    @Test
    public void restoreFromBackup_withValidBackup_restoresSuccessfully() throws IOException {
        // Create backup
        Path backupPath = temporaryFolder.resolve("backup.json");
        modelManager.backupData(backupPath.toString());

        // Add a person and restore to verify the restore works
        modelManager.addPerson(ALICE);
        boolean restored = modelManager.restoreFromBackup();
        assertTrue(restored, "Restoration should be successful.");
        assertFalse(modelManager.hasPerson(ALICE), "Person should not exist after restoration.");
    }

    @Test
    public void cleanOldBackups_nullStorage_throwsIoException() {
        ModelManager modelWithoutStorage = new ModelManager(new AddressBook(), new UserPrefs(), null);

        assertThrows(IOException.class, () -> modelWithoutStorage.cleanOldBackups(5),
                "Expected IOException when storage is not initialized.");
    }

    @Test
    public void cleanOldBackups_storageNotInitialized_throwsIoException() {
        ModelManager modelWithoutStorage = new ModelManager(new AddressBook(), new UserPrefs(), null);
        IOException exception = assertThrows(IOException.class, (
                ) -> modelWithoutStorage.cleanOldBackups(5),
                "Expected IOException when storage is not initialized.");
        assertEquals("Storage is not initialized!", exception.getMessage());
    }

    @Test
    public void cleanOldBackups_validStorage_executesSuccessfully() throws IOException {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        ModelManager modelManagerWithStorage = new ModelManager(new AddressBook(), new UserPrefs(), storage);
        assertDoesNotThrow(() -> modelManagerWithStorage.cleanOldBackups(5),
                "Cleaning old backups with valid storage should not throw any exception.");
    }

}
