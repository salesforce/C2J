import java.util.*;


public class ODMParser {

    Map<String, String> classes;
    String packageName;


    public Map<String, String> Parse(List<String> yamlObjectDataModel, String packageName) {

        this.packageName=packageName;
        this.classes = new HashMap<String, String>();

        List<String> currentYaml = new ArrayList();

        for (String currentLine : yamlObjectDataModel) {
            if (currentLine.isBlank() || currentLine.isEmpty() || currentLine.startsWith("#"))
                continue;

            if (!currentYaml.isEmpty()) {
                if (!currentLine.startsWith("\t") && !currentLine.startsWith(" ")) {
                    GenerateClass(currentYaml);
                    currentYaml.clear();
                }
            }
            currentYaml.add(Escape(currentLine));
        }

        if (!currentYaml.isEmpty())
            GenerateClass(currentYaml);


        return classes;
    }

    private void GenerateClass(List<String> currentYaml) {
        var tokens = Escape(currentYaml.get(0)).split(" ");
        String className = tokens[0];
        String baseClass = tokens[1];
        this.classes.put(className, GenerateClass(className, baseClass, currentYaml.subList(1, currentYaml.size())));
    }

    private String Escape(String currentLine) {
        return currentLine.replace(":", "").stripLeading().trim().replace("-", "");
    }

    String GenerateClass(String className, String baseClass, List<String> classYaml) {
        var javaClass = Header(className, baseClass, classYaml);

        javaClass.append(DeclareMembers(classYaml));
        javaClass.append(GettersSetters(classYaml));

        javaClass.append(GenerateToString(className, classYaml));
        return javaClass.append("}\n").toString();
    }

    private static String GenerateToString(String className, List<String> classYaml) {
        /*
            @Override
            public String toString() {
                return "Employee{" +
                        "name='" + name + '\'' +
                        ", dept='" + dept + '\'' +
                        '}';
            }
        */

        StringBuilder toString = new StringBuilder(String.format("\n\t@Override\n\tpublic String toString() {\n\treturn \"  %s  {\"+\n", className));

        for (String currentLine : classYaml) {
            var tokens = currentLine.split(" ");
            String memberName = tokens[0];
            String memberType = tokens[1];
            String originalMemberName = tokens.length > 2 ? tokens[2] : memberName;


            if ("Integer String Double Date".contains(memberType))
                toString.append(String.format("\t\t\t (%s == null ? \"\":\" %s = ' \" + %s+\"'\")+\n", originalMemberName, originalMemberName, originalMemberName));
            else if ("int double".contains(memberType))
                toString.append(String.format("\t\t\t (%s == 0 ? \"\":\" %s = ' \" + %s+\"'\")+\n", originalMemberName, originalMemberName, originalMemberName));
            else
                toString.append(String.format("\t\t\t \" %s = ' \" + %s+\"'\"+\n", originalMemberName, originalMemberName, originalMemberName));
        }

        toString.append("\t\t'}';\n\t}\n");

        return toString.toString();

    }

    static void AddUsing(String predicate, String using, List<String> classYaml, StringBuilder javaClass) {
        if (classYaml.stream().filter(l -> l.contains(predicate)).findAny().isPresent())
            javaClass.append(String.format("import %s;\n", using));
    }

    private StringBuilder Header(String name, String baseClass, List<String> classYaml) {
        StringBuilder javaClass;
        javaClass = new StringBuilder(String.format("package %s; \n\n", this.packageName));

        javaClass.append("import com.fasterxml.jackson.annotation.JsonGetter;\n");
        javaClass.append("import com.fasterxml.jackson.annotation.JsonSetter;\n");
        javaClass.append("import org.jetbrains.annotations.Nullable;\n");



        //AddUsing("DayOfWeek", "Model.DayOfWeek", classYaml, javaClass);
        AddUsing("List", "java.util.List", classYaml, javaClass);
        AddUsing("ObjectNode", "com.fasterxml.jackson.databind.node.ObjectNode", classYaml, javaClass);
        AddUsing("LocalTime", "java.time.LocalTime", classYaml, javaClass);
        AddUsing("TimeZone", "java.util.TimeZone", classYaml, javaClass);
        AddUsing("RelatedObjectCollection", "Model.RelatedObjectCollection", classYaml, javaClass);

        AddUsing("LocalDateTime", "java.time.LocalDateTime", classYaml, javaClass);
        AddUsing("LocalDateTime", "java.time.LocalDate", classYaml, javaClass);
        AddUsing("OffsetDateTime", "com.fasterxml.jackson.databind.annotation.JsonSerialize", classYaml, javaClass);
        AddUsing("OffsetDateTime", "com.fasterxml.jackson.databind.annotation.JsonDeserialize", classYaml, javaClass);
        AddUsing("OffsetDateTime", "java.time.OffsetDateTime", classYaml, javaClass);

        if (baseClass.equals("Object"))
            javaClass.append(String.format("import %s;\n", "com.fasterxml.jackson.annotation.JsonIgnoreProperties"));


        javaClass.append("\n\n/***  THIS CLASS WAS AUTO GENERATED by the DSMParser app   ***/");
        if (baseClass.equals("Object"))
            javaClass.append("\n@JsonIgnoreProperties(ignoreUnknown = true)");

        javaClass.append(String.format("\npublic class %s", name));
        if (!baseClass.equals("Object"))
            javaClass.append(String.format(" extends %s ", baseClass));

        javaClass.append("{");
        return javaClass;
    }

   /*
   OUTPUT =>

 public class OperatingHours extends sObject {
	private String TimeZone;

	// Getters / Setters

	//  TimeZone
	@JsonGetter("TimeZone")
	public String getTimeZone() {
		return TimeZone;
	}

	@JsonSetter("TimeZone")
	public void setTimeZone(String TimeZone) {
		this.TimeZone = TimeZone;
	}
}    */

    private static String GettersSetters(List<String> classYaml) {

        StringBuilder accessors = new StringBuilder("\n\n\t// Getters / Setters\n");

        for (String currentLine : classYaml)
            GenerateGetterSetter(accessors, currentLine);

        return accessors.toString();
    }

    private static void GenerateGetterSetter(StringBuilder accessors, String currentLine) {
        var tokens = currentLine.split(" ");
        String memberName = tokens[0];
        String memberType = tokens[1];
        String originalMemberName = tokens.length > 2 ? tokens[2] : memberName;

        accessors.append(String.format("\n\t//  %s", memberName));
        if (tokens.length > 2)
            accessors.append(String.format("  --> [%s]", originalMemberName));

        if ("Integer Boolean Double".contains(memberType))
            accessors.append(String.format("  ; @Nullable  %s\n\t@Nullable", memberType));
        boolean date = memberType.equals("Date") || memberType.equals("LocalDateTime")||memberType.equals("OffsetDateTime");

        accessors.append(String.format("\n\t@JsonGetter(\"%s\")", memberName));
        if (date)
            accessors.append(" @JsonDeserialize(using = Model.CustomDateTimeDeserializer.class) ");
        accessors.append(String.format("\n\tpublic %s get%s() {\n\t\treturn %s;\n\t}\n", memberType, originalMemberName, originalMemberName));

        accessors.append(String.format("\t@JsonSetter(\"%s\")", memberName));
        if (date)
            accessors.append(" @JsonSerialize(using = Model.CustomDateTimeSerializer.class)");
        accessors.append(String.format("\n\tpublic void set%s(%s %s) {\n\t\tthis.%s = %s;\n\t}\n\n",
                originalMemberName, memberType, originalMemberName, originalMemberName, originalMemberName));
    }

    private static String DeclareMembers(List<String> classYaml) {
        StringBuilder members = new StringBuilder("\n");
        for (String currentLine : classYaml) {
            try {
                var tokens = currentLine.split(" ");
                String memberName = tokens[0];
                String memberType = tokens[1];
                String originalMemberName = tokens.length > 2 ? tokens[2] : memberName;

                members.append(String.format("\tprivate %s %s;\n", memberType, originalMemberName));
            } catch (Exception e) {
                System.out.println(currentLine);
                e.printStackTrace();
            }
        }

        return members.toString();
    }
}
