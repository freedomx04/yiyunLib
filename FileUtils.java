package itec.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtils {

    static Log log = LogFactory.getLog(FileUtils.class);

    private List<String> list = new ArrayList<String>();
    public List<String> getFileList(File dir, String... suffixs) {
        if (dir.isDirectory()) {
            File[] fileList = dir.listFiles();
            for (File file: fileList) {
                if (file.isDirectory()) {
                    getFileList(file, suffixs);
                } else {
                    for (String suffix : suffixs) {
                        String lowname = file.getName().toLowerCase();
                        if (lowname.endsWith(suffix.toLowerCase())) {
                            list.add(file.getAbsolutePath());
                        }
                    }
                }
            }
        } else {
            for (String suffix : suffixs) {
                String lowname = dir.getName().toLowerCase();
                if (lowname.endsWith(suffix.toLowerCase())) {
                    list.add(dir.getAbsolutePath());
                }
            }
        }
        return list;
    }

    public static List<String> getFileRange(File dir, String start, String end) {
        List<String> list = new ArrayList<String>();
        File[] fileList = dir.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            String name = fileList[i].getName();
            if (name.equals(start)) {
                for (int j = i; j < fileList.length; j++) {
                    name = fileList[j].getName();
                    list.add(dir + File.separator + name);
                    if (name.equals(end)) {
                        return list;
                    }
                }
            }
        }
        return list;
    }

    public static List<String> getListRange(List<String> list, String start) {
        List<String> retList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String line = list.get(i);
            if (line.contains(start)) {
                for (int j = i; j < list.size(); j++) {
                    retList.add(list.get(j));
                }
                break;
            }
        }
        return retList;
    }
    
    public static void writeInfo(File file, String data, boolean append) throws IOException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        try {
            fw = new FileWriter(file, append);
            bw = new BufferedWriter(fw);
            bw.write(data);
            bw.newLine();
            bw.flush();
        } finally {
            bw.close();
            fw.close();
        }
    }
    
    public static void makeDirs(Path path) {
        File file = path.toFile();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }
    
    public static void sureDirExists(File file, boolean parent) throws IOException {
        File dir = parent ? file.getParentFile() : file;
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("create folder failed: " + dir);
            }
        }
    }
}
