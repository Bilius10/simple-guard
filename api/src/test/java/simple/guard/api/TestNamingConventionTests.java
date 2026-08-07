package simple.guard.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class TestNamingConventionTests {

    private static final Path TEST_SOURCE_ROOT = Path.of("src", "test", "java");
    private static final Pattern TYPE_DECLARATION = Pattern.compile(
            "\\b(?:public\\s+|protected\\s+|private\\s+)?(?:final\\s+|abstract\\s+)?(?:class|interface|enum|record)\\s+(\\w+)"
    );
    private static final Pattern TEST_METHOD_DECLARATION = Pattern.compile(
            "@Test\\s+(?:\\R\\s*)+(?:public\\s+|protected\\s+|private\\s+)?(?:final\\s+)?(?:void|[\\w<>?,\\s]+)\\s+(\\w+)\\s*\\("
    );

    @Test
    void testClassesAndMethodsEndWithTestsTests() throws IOException {
        List<String> violations = new ArrayList<>();

        try (var files = Files.walk(TEST_SOURCE_ROOT)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> collectViolations(path, violations));
        }

        assertThat(violations).isEmpty();
    }

    private static void collectViolations(Path path, List<String> violations) {
        try {
            String source = Files.readString(path);
            Matcher testMethods = TEST_METHOD_DECLARATION.matcher(source);
            boolean hasTestMethod = false;

            while (testMethods.find()) {
                hasTestMethod = true;
                String methodName = testMethods.group(1);
                if (!methodName.endsWith("Tests")) {
                    violations.add(path + " method " + methodName + " must end with Tests");
                }
            }

            if (hasTestMethod) {
                Matcher type = TYPE_DECLARATION.matcher(source);
                if (type.find() && !type.group(1).endsWith("Tests")) {
                    violations.add(path + " class " + type.group(1) + " must end with Tests");
                }
            }
        } catch (IOException exception) {
            violations.add(path + " could not be inspected: " + exception.getMessage());
        }
    }
}
