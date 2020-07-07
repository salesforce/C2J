
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class Main {

    // Parse YAML file. As response, generate Java files
    static final String YAML_FILE = System.getProperty("user.dir") + "/dataModel.yaml";
    static final String OUTPUT = String.format("%s/../Model/src/main/java/Model/", System.getProperty("user.dir"));

    public static void main(String[] args) {

        ODMParser p = new ODMParser();

        List<String> yamlObjectDataModel = ReadYaml(args);

        try {
            Map<String, String> javaClasses =
                    p.Parse(Objects.requireNonNull(yamlObjectDataModel));

            PersistClasses(javaClasses, OUTPUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void PersistClasses(Map<String, String> javaClasses, String outputFolder) {
        File file = new File(outputFolder);
        if (!file.exists())
            file.mkdir();

        for (String currentClass : javaClasses.keySet()) {
            String filename = String.format("%s%s.java", outputFolder, currentClass);
            System.out.println(String.format("%s - %s", currentClass, filename));
            WriteODMClass(filename, javaClasses.get(currentClass));
        }
    }

    static void WriteODMClass(String filename, String javaSrc) {
        File f = new File(filename);
        if (f.exists())
            f.delete();

        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename)));
            out.println(javaSrc);
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static List<String> ReadYaml(String[] args) {

        String fName = args.length > 0 ? args[0] : YAML_FILE;
        System.out.println(fName);

        try {
            return ReadLines(fName);
        } catch (IOException e) {
            try {
                return ReadLines(YAML_FILE);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return null;
            }
        }
    }

    private static List<String> ReadLines(String fileName) throws IOException {
        return Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
    }
}
