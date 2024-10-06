package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code BackupCommand}.
 */
public class BackupCommandTest {

    @TempDir
    public Path temporaryFolder;
    private Model model;
    private Model expectedModel;

    /**
     * Sets up the test environment with the required model and storage.
     */
    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        model = new ModelManager(getTypicalAddressBook(), new UserPrefs(), storage);

        JsonAddressBookStorage addressBookExpectedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBookExpected.json"));
        JsonUserPrefsStorage userPrefsExpectedStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefsExpected.json"));
        StorageManager expectedStorage = new StorageManager(addressBookExpectedStorage, userPrefsExpectedStorage);

        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs(), expectedStorage);
    }

    /**
     * Tests the successful backup command execution with added patients.
     *
     * @throws Exception if the execution of the command fails.
     */
    @Test
    public void executeBackupCommand_withAddedPatients_success() throws Exception {
        // Step 1: Add new patient records using AddCommand
        Person newPerson = new PersonBuilder().withName("John Doe").withPhone("98765432")
                .withAddress("123 Baker Street").withEmail("john.doe@example.com").build();
        AddCommand addCommand = new AddCommand(newPerson);
        addCommand.execute(model); // Add to the actual model

        // Also add to the expected model
        expectedModel.addPerson(newPerson);

        // Step 2: Execute BackupCommand
        String backupFilePath = temporaryFolder.resolve("backup.json").toString();
        BackupCommand backupCommand = new BackupCommand(backupFilePath);

        String expectedMessage = String.format(BackupCommand.MESSAGE_SUCCESS, backupFilePath);

        // Step 3: Verify backup command success
        assertCommandSuccess(backupCommand, model, expectedMessage, expectedModel);

        // Step 4: Verify that the backup file has been created
        assertTrue(Path.of(backupFilePath).toFile().exists());
    }

    /**
     * Tests the failure of the backup command when the path is null.
     */
    @Test
    public void executeBackupCommand_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new BackupCommand(null));
    }

}
