package itec.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CompressUtils {

    static Log log = LogFactory.getLog(CompressUtils.class);

    /**
     * compress file endWith suffix tar
     * 
     * @param sourcePath
     * @param targetPath
     */
    public static void compressTarFile(String sourcePath, String targetPath) {
        FileInputStream fis = null;
        ArchiveInputStream in = null;
        BufferedInputStream bin = null;
        BufferedOutputStream bout = null;
        try {
            fis = new FileInputStream(new File(sourcePath));
            in = new ArchiveStreamFactory().createArchiveInputStream("tar", fis);
            bin = new BufferedInputStream(in);
            TarArchiveEntry entry = (TarArchiveEntry) in.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                String[] names = name.split("/");
                String fileName = targetPath;
                for (int i = 0; i < names.length; i++) {
                    String str = names[i];
                    fileName = fileName + File.separator + str;
                }

                if (name.endsWith("/")) {
                    mkFolder(fileName);
                } else {
                    File file = mkFile(fileName);
                    bout = new BufferedOutputStream(new FileOutputStream(file));
                    int b;
                    while ((b = bin.read()) != -1) {
                        bout.write(b);
                    }
                    bout.flush();
                    bout.close();
                }
                entry = (TarArchiveEntry) in.getNextEntry();
            }
        } catch (IOException | ArchiveException e) {
            log.debug(e, e);
        } finally {
            try {
                if (bin != null) {
                    bin.close();
                }
            } catch (IOException e) {
                log.debug(e, e);
            }
        }
    }

    /**
     * compress file endWith suffix zip
     * 
     * @param sourcePath
     * @param targetPath
     */
    public static void compressZipFile(String sourcePath, String targetPath) {
        FileInputStream fis = null;
        ZipInputStream zis = null;
        FileOutputStream fos = null;
        ZipEntry entry = null;
        try {
            File sourceFile = new File(sourcePath);
            fis = new FileInputStream(sourceFile);
            zis = new ZipInputStream(fis);
            while ((entry = zis.getNextEntry()) != null) {
                int count = 0;
                byte[] data = new byte[1024];

                String target = targetPath + File.separator + entry.getName();
                if (target.endsWith("/")) {
                    mkFolder(target);
                } else {
                    File file = mkFile(target);
                    fos = new FileOutputStream(file);
                    while ((count = zis.read(data, 0, 1024)) != -1) {
                        fos.write(data, 0, count);
                    }
                    fos.flush();
                    fos.close();
                }
            }
        } catch (IOException e) {
            log.debug(e, e);
        } finally {
            try {
                fis.close();
                zis.close();
            } catch (Exception e) {
               log.debug(e, e);
            }
        }
    }

    public static void mkFolder(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static File mkFile(String fileName) {
        File file = new File(fileName);
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        } catch (IOException e) {
            log.debug(e, e);
        }
        return file;
    }
}
