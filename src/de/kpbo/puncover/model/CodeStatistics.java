package de.kpbo.puncover.model;

import com.google.gson.stream.JsonReader;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by karl on 07/09/14.
 */
public class CodeStatistics {

    public static final String FILE_NAME  = ".gutter.json";

    public static CodeStatistics read(VirtualFile baseDir) {

        VirtualFile jsonFile = baseDir.findChild(FILE_NAME);
        if (jsonFile == null) {
            return null;
        }

        CodeStatistics statistics = null;

        try {
            Reader reader = new FileReader(jsonFile.getPath());

            Date timestamp = null;
            HashMap<String, CodeFile> files = new HashMap<String, CodeFile>();

            JsonReader jsonReader = new JsonReader(reader);
            jsonReader.beginObject();

            while (jsonReader.hasNext()) {

                String name = jsonReader.nextName();



                if ("meta".equals(name)) {
                    // TODO: read timestamp

                    jsonReader.beginObject();

                    name = jsonReader.nextName();
                    if ("timestamp".equals(name)) {

                        String timestampString = jsonReader.nextString();

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                        try {
                            timestamp = dateFormat.parse(timestampString);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }

                    jsonReader.endObject();

                } else if ("symbols_by_file".equals(name)) {

                    jsonReader.beginObject();

                    while (jsonReader.hasNext()) {

                        String path = jsonReader.nextName();

                        CodeFile codeFile = new CodeFile(path);

                        Map<Integer, CodeAnnotation> annotations = new HashMap<Integer, CodeAnnotation>();

                        jsonReader.beginArray();

                        while (jsonReader.hasNext()) {

                            jsonReader.beginObject();

                            CodeAnnotation annotation = new CodeAnnotation();

                            while (jsonReader.hasNext()) {

                                name = jsonReader.nextName();
                                if ("line".equals(name)) {
                                    annotation.setLineNumber(jsonReader.nextInt());
                                } else if ("short_text".equals(name)) {
                                    annotation.setShortText(jsonReader.nextString());
                                } else if ("long_text".equals(name)) {
                                    annotation.setLongText(jsonReader.nextString());
                                } else if ("name".equals(name)) {
                                    annotation.setName(jsonReader.nextString());
                                }

                            }

                            codeFile.addAnnotation(annotation);

                            jsonReader.endObject();

                        }

                        jsonReader.endArray();

                        files.put(path, codeFile);

                    }

                    jsonReader.endObject();
                }
            }

            jsonReader.endObject();

            statistics = new CodeStatistics();
            statistics.timestamp = timestamp;
            statistics.files = files;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return statistics;
    }

    private Date timestamp;
    private Map<String, CodeFile> files;

    public CodeFile getCodeFileForPath(String path) {
        if (path == null) {
            return null;
        }

        return files.get(path);
    }


}
