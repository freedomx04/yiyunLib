package itec.patent.data.utils;

import itec.patent.data.param.PatentFile;
import itec.patent.data.utils.ImageUtils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class PDFUtils {

    static Log log = LogFactory.getLog(PDFUtils.class);

    public static void addImageToPDFbyITEXT(Document pdfdoc, Path imagepath, Rectangle rect) throws DocumentException, MalformedURLException, IOException  {
        String lowname = imagepath.toString().toLowerCase();
        Image img = Image.getInstance(lowname);
        img.scaleToFit(rect.getWidth() - 10, rect.getHeight() - 10);
        pdfdoc.newPage();
        pdfdoc.add(img);
    }

    public static void mergeFirstPagePDFs(Document pdfdoc, PdfWriter writer, Path firstPagePdfPath) throws IOException {
        FileInputStream pdfStream = new FileInputStream(firstPagePdfPath.toString());
        PdfReader inputPDF = new PdfReader(pdfStream);
        PdfContentByte cb = writer.getDirectContent(); // Holds the PDF data
        PdfImportedPage page;
        pdfdoc.newPage();
        page = writer.getImportedPage(inputPDF, 1);
        cb.addTemplate(page, 0, 0);
        inputPDF.close();
        pdfStream.close();
    }

    //split first page of pdf
    public static void splitFirstPagePDF(PatentFile pfile) throws IOException {
        if (!pfile.file.exists()) {
            return;
        }
        File outFile = ImageUtils.getCacheFile(pfile, Paths.get("firstPage.pdf"));
        FileUtils.sureDirExists(outFile.getParentFile(), false);
        //with image magick
        LinkedList<String> cmd = new LinkedList<String>();
        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            cmd.add("cmd.exe");
            cmd.add("/C");
        }
        cmd.add("convert");
        cmd.add("-density");
        cmd.add("400");
        cmd.add(pfile.file.getAbsolutePath() + "[0]");
        cmd.add(outFile.getAbsolutePath());
        ProcessBuilder pb=new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process=pb.start();
        BufferedReader inStreamReader = new BufferedReader(
                new InputStreamReader(process.getInputStream())); 
        String line = "";
        while((line = inStreamReader.readLine()) != null){
            log.error("imagemagic meg : " + line);
        }
    }

    //split pdf to png with image magick
    public static void splitPDFToImage(PatentFile pfile, int num) throws IOException  {
        if (!pfile.file.exists()) {
            return;
        }
        File outdir = ImageUtils.getCacheFile(pfile, Paths.get("fullImage"));
        FileUtils.sureDirExists(outdir, false);
        LinkedList<String> cmd = new LinkedList<String>();
        //using command line in windows
        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            cmd.add("cmd.exe");
            cmd.add("/C");
        //using super user in linux
        } else {
            cmd.add("sudo");
        }
        cmd.add("convert");
        cmd.add("-density");
        cmd.add("400");
        cmd.add("-resize");
        cmd.add("25%");
        cmd.add(pfile.file.getAbsolutePath() + "[" + (num-1) + "]");
        cmd.add(outdir.getAbsolutePath() + File.separator + num + ".png");
        ProcessBuilder pb=new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process=pb.start();
        BufferedReader inStreamReader = new BufferedReader(
                new InputStreamReader(process.getInputStream())); 
        String line = "";
        while((line = inStreamReader.readLine()) != null){
            log.error("imagemagic meg : " + line);
        }
    }

}
