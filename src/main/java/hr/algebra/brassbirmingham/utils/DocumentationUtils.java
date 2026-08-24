package hr.algebra.brassbirmingham.utils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class DocumentationUtils {

    private static final String PATH_WITH_CLASSES = "target/classes/";
    private static final String CLASS_FILE_NAME_EXTENSION = ".class";
    private static final String HTML_DOCUMENTATION_FILE_NAME = "doc/documentation.html";

    public static void generateDocumentation() throws IOException {
        Path start = Paths.get(PATH_WITH_CLASSES);
        try(Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE)){
            List<String> classList = stream
                    .filter(f -> f.getFileName()
                            .toString()
                            .endsWith(CLASS_FILE_NAME_EXTENSION)
                    && Character.isUpperCase(f.getFileName().toString().charAt(0))
                    && !f.getFileName().toString().contains("$"))
                    .map(String::valueOf)
                    .sorted()
                    .toList();

            String htmlString = generateHtmlDocumentationCode(classList);
            Files.writeString(Path.of(HTML_DOCUMENTATION_FILE_NAME), htmlString);
        }
    }

    private static String generateHtmlDocumentationCode(List<String> classList) {

        StringBuilder htmlBuilder = new StringBuilder();
        String htmlStart = """
                <!DOCTYPE html>
                <html>
                <head>
                <title>Java documentation</title>
                </head>
                <body>
                
                <h1>List of classes:</h1>""";
        htmlBuilder.append(htmlStart);

        for(String className : classList){
            className = className.substring(PATH_WITH_CLASSES.length(), className.length() - CLASS_FILE_NAME_EXTENSION.length())
                    .replace("\\", ".");

            try{
                Class<?> clazz = Class.forName(className);
                htmlBuilder.append("<h1>Class: ").append(className).append("</h1><br />");

                if(clazz.getConstructors().length > 0){
                    htmlBuilder.append("<h2>List of constructors:</h2> <br />");

                    for(Constructor<?> constructor : clazz.getConstructors()){
                        htmlBuilder.append("<h3> Constructor: ").append(constructor).append("</h3><br />");
                    }
                }
                else {
                    htmlBuilder.append("<h2>No constructors</h2>");
                }
            } catch (ClassNotFoundException e){
                throw new RuntimeException(e);
            }
        }

        String htmlEnd = """
                </body>
                </html>""";
        htmlBuilder.append(htmlEnd);

        return htmlBuilder.toString();
    }
}

