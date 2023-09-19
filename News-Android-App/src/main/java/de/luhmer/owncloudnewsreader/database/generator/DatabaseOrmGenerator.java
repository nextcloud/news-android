package de.luhmer.owncloudnewsreader.database.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.daogenerator.DaoGenerator;

public class DatabaseOrmGenerator {
    private static final String SCHEMA_OUTPUT_DIR = "./News-Android-App/src/main/java/";

    /**
     * Generator main application which builds all of the schema versions
     * (including older versions used for migration test purposes) and ensures
     * business rules are met; these include ensuring we only have a single
     * current schema instance and the version numbering is correct.
     */
    public static void main(String[] args) throws Exception {
        List<SchemaVersion> versions = new ArrayList<>();

        versions.add(new LastestVersion(true));

        validateSchemas(versions);

        for (SchemaVersion version : versions) {
            // NB: Test output creates stubs, we have an established testing
            // standard which should be followed in preference to generating
            // these stubs.
            new DaoGenerator().generateAll(version.getSchema(),
                    SCHEMA_OUTPUT_DIR);
        }
    }

    /**
     * Validate the schema, throws
     *
     * @throws IllegalArgumentException
     *             if data is invalid
     */
    public static void validateSchemas(List<SchemaVersion> versions)
            throws IllegalArgumentException {
        int numCurrent = 0;
        Set<Integer> versionNumbers = new HashSet<>();

        for (SchemaVersion version : versions) {
            if (version.isCurrent()) {
                numCurrent++;
            }

            int versionNumber = version.getVersionNumber();
            if (versionNumbers.contains(versionNumber)) {
                throw new IllegalArgumentException(
                        "Unable to process schema versions, multiple instances with version number : "
                                + version.getVersionNumber());
            }
            versionNumbers.add(versionNumber);
        }

        if (numCurrent != 1) {
            throw new IllegalArgumentException(
                    "Unable to generate schema, exactly one schema marked as current is required.");
        }
    }
}
