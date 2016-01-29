package itec.patent.data.utils;

import itec.patent.data.param.DataParamContext;
import itec.patent.data.param.PatentFile;
import itec.patent.data.param.PatentPath;
import itec.patent.mongodb.PatentInfo2;
import itec.patent.mongodb.Pto;
import itec.patent.solr.PatentWeb;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.tsaikd.java.utils.WebClient;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

public class ImageUtils {

    static Log log = LogFactory.getLog(ImageUtils.class);

    protected static File getCacheFile(PatentFile pfile, Path filepath) {
        String relpath = DataParamContext.getRelPatentPath(pfile.info);

        for (File file : PatentDataConfig.cachepathPtoMap.get(pfile.info.pto)) {
            return file.toPath()
                .resolve(relpath)
                .resolve(filepath)
                .toFile();
        }

        return new File("");
    }

    protected static void saveImage(BufferedImage image, File file) throws IOException {
        FileUtils.sureDirExists(file, true);
        if (!ImageIO.write(image, "PNG", file)) {
            throw new IOException("create cache image failed: " + file);
        }
    }

    @SuppressWarnings("serial")
    protected static HashMap<String, String> extMimeTypeMap = new HashMap<String, String>() {{
        put("gif", "gif");
        put("jpg", "jpeg");
        put("png", "png");
        put("tif", "tiff");
    }};
    protected static void checkImageMimeType(PatentFile pfile) {
        // auto detect type failed
        if ((pfile.mimeType == null || pfile.mimeType.equalsIgnoreCase("text/plain")) && pfile.file.exists()) {
            // fix mimetype by file name extension
            String ext = FilenameUtils.getExtension(pfile.file.getName()).toLowerCase();
            if (extMimeTypeMap.containsKey(ext)) {
                pfile.mimeType = "image/" + extMimeTypeMap.get(ext);
            }
        }
    }

    /**
     * @param filepath relative to patent directory, like: "fullImage/1.png"
     */
    protected static void cachePatentImage2Png(PatentFile pfile, Path filepath) throws IOException {
        if (!pfile.file.exists()) {
            return;
        }
        if (pfile.mimeType.equalsIgnoreCase("image/png")) {
            return;
        }

        File cacheFile = getCacheFile(pfile, filepath);
//        RenderedOp jai = JAI.create("fileload", pfile.file.getPath());
//        BufferedImage image = jai.getAsBufferedImage();
//        saveImage(image, cacheFile);
        FileUtils.sureDirExists(cacheFile, true);
        LinkedList<String> cmd = new LinkedList<String>();
        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            cmd.add("cmd.exe");
            cmd.add("/C");
        } else {
            cmd.add("sudo");
        }
        cmd.add("convert");
        cmd.add("-density");
        cmd.add("400");
        cmd.add(pfile.file.getAbsolutePath());
        cmd.add(cacheFile.getAbsolutePath());
        ProcessBuilder pb=new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process=pb.start();
        BufferedReader inStreamReader = new BufferedReader(
                new InputStreamReader(process.getInputStream())); 
        String line = "";
        while((line = inStreamReader.readLine()) != null){
            log.error("imagemagic meg : " + line);
        }
        pfile.file = cacheFile;
        pfile.mimeType = "image/png";
    }

    /**
     * @param filepath relative to patent directory, like: "fullImage/1.png"
     */
    protected static void cacheAndResizePatentImage2Png(PatentFile pfile, Path filepath, int max_width, int max_height) throws IOException {
        int[] size = getImageSize(pfile.file.getAbsolutePath());
        if (!pfile.file.exists()) {
            return;
        }
        if (pfile.mimeType.equalsIgnoreCase("image/png") && size[0] <= max_width && size[1] <= max_height) {
            return;
        }

        File cacheFile = getCacheFile(pfile, filepath);
        FileUtils.sureDirExists(cacheFile, true);
        LinkedList<String> cmd = new LinkedList<String>();
        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            cmd.add("cmd.exe");
            cmd.add("/C");
        } else {
            cmd.add("sudo");
        }
        cmd.add("convert");
        //resize 在把 tif 大圖轉成 png 小圖的時候會嚴重失真 (參考 ep firstImage)，而 thumbnail 不會
        //cmd.add("-resize");
        cmd.add("-thumbnail");
        cmd.add(max_width + "X" + max_height);
        cmd.add(pfile.file.getAbsolutePath());
        cmd.add(cacheFile.getAbsolutePath());
        ProcessBuilder pb=new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process=pb.start();
        BufferedReader inStreamReader = new BufferedReader(
                new InputStreamReader(process.getInputStream())); 
        String line = "";
        while((line = inStreamReader.readLine()) != null){
            log.error("imagemagic meg : " + line);
        }
        inStreamReader.close();
        pfile.file = cacheFile;
        pfile.mimeType = "image/png";
    }

    protected static int[] getImageSize(String imagePath) throws IOException {
        int[] size = new int[2];
        LinkedList<String> cmd = new LinkedList<String>();
        if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            cmd.add("cmd.exe");
            cmd.add("/C");
        } else {
            cmd.add("sudo");
        }
        cmd.add("identify");
        cmd.add(imagePath);
        ProcessBuilder pb=new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process process=pb.start();
        BufferedReader inStreamReader = new BufferedReader(
                new InputStreamReader(process.getInputStream())); 
        String line = inStreamReader.readLine();
        if(line != null) {
            String[] size_str = line.split(" ")[2].split("x");
            size[0] = Integer.parseInt(size_str[0]);
            size[1] = Integer.parseInt(size_str[1]);
        }
        inStreamReader.close();
        return size;
    }
    
    protected static class FileUtils {
        //sort filename by pagenumber
        protected static class PageComparator implements Comparator<String> {
            public int compare(String filename1, String filename2) {
                int filepage1 =Integer.parseInt(filename1.substring(0, filename1.indexOf(".")));
                int filepage2 =Integer.parseInt(filename2.substring(0, filename2.indexOf(".")));
                if (filepage1 > filepage2)
                    return 1;
                else
                    return -1;
            }
        }
        protected static Comparator<String> pageComparator = new PageComparator();

        protected static void sureDirExists(File file, boolean parent) throws IOException {
            File dir = parent ? file.getParentFile() : file;
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    if (!dir.exists()) {
                        throw new IOException("create folder failed: " + dir);
                    }
                }
            }
        }

        protected static LinkedList<Path> listPatentFilesByPrefix(PatentPath patpath, Path subdir,
                final String... prefixes) {
            LinkedList<Path> list = new LinkedList<>();

            for (Path path : patpath.paths) {
                Path dirPath = path.resolve(subdir);
                String[] filenames = dirPath.toFile().list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        String lowname = name.toLowerCase();
                        for (String prefix : prefixes) {
                            if (lowname.startsWith(prefix.toLowerCase())) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
                if (filenames == null || filenames.length < 1) {
                    continue;
                }


                for (String filename : filenames) {
                    list.add(dirPath.resolve(filename));
                }
                break;
            }

            return list;
        }

        protected static LinkedList<Path> listPatentFilesBySuffix(PatentPath patpath, Path subdir,
                final String... suffixes) {
            LinkedList<Path> list = new LinkedList<>();

            for (Path path : patpath.paths) {
                Path dirPath = path.resolve(subdir);
                String[] filenames = dirPath.toFile().list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        String lowname = name.toLowerCase();
                        for (String suffix : suffixes) {
                            if (lowname.endsWith(suffix.toLowerCase())) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
                if (filenames == null || filenames.length < 1) {
                    continue;
                }
                Arrays.sort(filenames, pageComparator);
                for (String filename : filenames) {
                    list.add(dirPath.resolve(filename));
                }
                break;
            }

            return list;
        }

    }

    public static class WebUtils {

        /**
         * @return true if download success
         */
        protected static boolean downloadFileFromWeb(String url, File output) throws IOException {
            boolean ret = false;

            FileUtils.sureDirExists(output, true);

            CloseableHttpClient httpClient = WebClient.newHttpClient();
            HttpGet method = new HttpGet(url);
            HttpResponse response = httpClient.execute(method);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() < 400) {
                InputStream is = entity.getContent();
                FileOutputStream fos = new FileOutputStream(output);
                try {
                    IOUtils.copy(is, fos);
                    ret = true;
                } catch (IOException e) {
                    if (fos != null) {
                        fos.close();
                        fos = null;
                    }
                } finally {
                    if (fos != null) {
                        fos.close();
                        fos = null;
                    }
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                }
            } else {
                EntityUtils.consume(entity);
            }
            httpClient.close();

            if (!ret) {
                output.delete();
            }

            return ret;
        }

        protected static String downloadPageFromWeb(String url) throws ClientProtocolException, IOException {
            String ret = "";

            CloseableHttpClient httpClient = WebClient.newHttpClient();
            HttpGet method = new HttpGet(url);
            HttpResponse response = httpClient.execute(method);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() < 400) {
                ret = EntityUtils.toString(entity);
            } else {
                EntityUtils.consume(entity);
            }
            httpClient.close();

            return ret;
        }

        protected static String downloadPageFromWeb(String url, HttpEntity postEntity) throws ClientProtocolException, IOException {
            String ret = "";

            CloseableHttpClient httpClient = WebClient.newHttpClient();
            HttpPost method = new HttpPost(url);
            method.setEntity(postEntity);
            HttpResponse response = httpClient.execute(method);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() < 400) {
                ret = EntityUtils.toString(entity);
            } else {
                EntityUtils.consume(entity);
            }
            httpClient.close();

            return ret;
        }

    }

    public static class CniprPatentUtils {

        protected static File getCniprFile(PatentFile pfile, Path filepath) {
            String relpath = DataParamContext.getRelPatentPath(pfile.info);

            for (File file : PatentDataConfig.cniprpathPtoMap.get(pfile.info.pto)) {
                return file.toPath()
                    .resolve(relpath)
                    .resolve(filepath)
                    .toFile();
            }

            return new File("");
        }

        protected static PatentWeb getPatentWebFromCnipr(PatentInfo2 info) throws IOException {
            Pto pto;
            String url;
            HttpEntity entity;
            String id;

            switch (info.pto) {
            case CN:
            case CNIPR:
                if (info.type.equalsIgnoreCase("外观专利")) {
                    pto = Pto.CNIPR;
                    LinkedList<BasicNameValuePair> params = new LinkedList<>();
                    params.add(new BasicNameValuePair("strWhere", "申请号=(CN" + info.patentNumber + ")"));
                    params.add(new BasicNameValuePair("recordCursor", "0"));
                    params.add(new BasicNameValuePair("strSources", "WGZL"));
                    entity = new UrlEncodedFormEntity(params, "UTF-8");
                    url = "http://search.cnipr.com/search!doDetailSearch.action";
                    id = url + " <!> " + info.patentNumber;
                } else {
                    throw new UnsupportedEncodingException("unsupported CN type for PatentWeb from CNIPR: " + info.type);
                }
                break;
            default:
                throw new UnsupportedEncodingException("unsupported pto for PatentWeb from CNIPR: " + info.pto);
            }

            try {
                HttpSolrServer solr = PatentDataConfig.patentwebSolr;
                SolrQuery query = new SolrQuery("url:\"" + id + "\"");
                QueryResponse res = solr.query(query);
                List<PatentWeb> pwebs = res.getBeans(PatentWeb.class);
                if (!pwebs.isEmpty()) {
                    return pwebs.get(0);
                }

                String page = WebUtils.downloadPageFromWeb(url, entity);
                if (!page.isEmpty()) {
                    PatentWeb pweb = new PatentWeb();
                    pweb.url = id;
                    pweb.pto_sl = pto.toString();
                    pweb.page_s = page;
                    pweb.provider_sl = "CNIPR";
                    pweb.do_date = new Date();
                    solr.addBean(pweb);
                    return pweb;
                }
            } catch (SolrServerException e) {
                throw new IOException(e);
            }

            return null;
        }

//        public static void Test_getPatentWebFromCnipr() throws IOException {
//            PatentInfo2 info = PatentInfo2.findOne(Pto.CN, "52131e0773ad1076aa572db3", "pto", "stat", "kindcode", "doDate", "patentNumber", "openNumber", "decisionNumber", "filePageNumber", "type");
//            getPatentWebFromCnipr(info);
//        }

        @SuppressWarnings("serial")
        protected static HashMap<String, String> cniprTypeMap = new HashMap<String, String>() {{
            put("发明专利1", "fm");
            put("发明专利2", "sq");
            put("实用新型1", "xx");
            put("实用新型2", "xx");
            put("外观专利1", "WG");
            put("外观专利2", "WG");
        }};
        protected static PatentFile downloadFirstImageFromCnipr(PatentPath patpath) throws IOException {
            PatentFile pfile = new PatentFile();
            PatentInfo2 info = pfile.info = patpath.info;

            switch (info.pto) {
            case CN:
            case CNIPR:
                break;
            default:
                throw new UnsupportedEncodingException("unsupported pto for download first image from CNIPR");
            }

            String cniprtype = info.type + info.stat.toString();
            if (!cniprTypeMap.containsKey(cniprtype)) {
                throw new UnsupportedEncodingException("unsupported type for download first image from CNIPR");
            }

            File file;
            file = getCniprFile(pfile, Paths.get("firstImage.gif"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String url = "http://pic.cnipr.com:8080/XmlData" +
                    "/" + cniprTypeMap.get(cniprtype) +
                    "/" + sdf.format(info.doDate) +
                    "/" + info.patentNumber +
                    "/" + info.patentNumber.replaceAll("\\.\\S$", "") +
                    ".gif";
            if (WebUtils.downloadFileFromWeb(url, file)) {
                pfile.file = file;
                pfile.mimeType = "image/gif";
            } else if (info.stat == 2 && info.openDate != null) {
                cniprtype = info.type + "1";
                url = "http://pic.cnipr.com:8080/XmlData" +
                        "/" + cniprTypeMap.get(cniprtype) +
                        "/" + sdf.format(info.openDate) +
                        "/" + info.patentNumber +
                        "/" + info.patentNumber.replaceAll("\\.\\S$", "") +
                        ".gif";
                if (WebUtils.downloadFileFromWeb(url, file)) {
                    pfile.file = file;
                    pfile.mimeType = "image/gif";
                }
            }

            return pfile;
        }

    }

    public static class GooglePatentUtils {

        protected static File getGooglePatentFile(PatentFile pfile, Path filepath) {
            String relpath = DataParamContext.getRelPatentPath(pfile.info);

            for (File file : PatentDataConfig.googlepatentpathPtoMap.get(pfile.info.pto)) {
                return file.toPath()
                    .resolve(relpath)
                    .resolve(filepath)
                    .toFile();
            }

            return new File("");
        }

        protected static String getGooglePatentNumber(PatentInfo2 info) throws UnsupportedEncodingException {
            String pn;
            switch (info.pto){
            case US:
            case USPTO:
                pn = info.patentNumber.replaceAll("(?i)^(US)?0*", "US");
                break;
            case CN:
            case CNIPR:
                if (info.stat == 1) {
                    pn = info.openNumber.replaceAll("(?i)^(CN)?0*", "CN");
                } else {
                    pn = info.decisionNumber.replaceAll("(?i)^(CN)?0*", "CN");
                }
                pn = pn.replaceAll("\\D$", "");
                if (info.kindcode != null) {
                    pn += info.kindcode.toUpperCase();
                }
                break;
            case EP:
            case EPO:
                pn = info.patentNumber.replaceAll("(?i)^(EP)?0*", "EP");
                pn = pn.replaceAll("\\D\\d$", "");
                if (info.kindcode != null) {
                    pn += info.kindcode.toUpperCase();
                }
                break;
            default:
                throw new UnsupportedEncodingException("unsupported pto for google patent: " + info.pto);
            }
            return pn;
        }

        protected static PatentWeb getPatentWebFromGooglePatent(PatentInfo2 info) throws IOException {
            Pto pto;
            String url;
            String pn = getGooglePatentNumber(info);

            switch (info.pto) {
            case US:
            case USPTO:
                pto = Pto.USPTO;
                url = "http://www.google.com/patents/" + pn;
                break;
            case CN:
            case CNIPR:
                pto = Pto.CNIPR;
                url = "http://www.google.com/patents/" + pn + "?cl=zh";
                break;
            case EP:
            case EPO:
                pto = Pto.EPO;
                url = "http://www.google.com/patents/" + pn + "?cl=en";
                break;
            default:
                throw new UnsupportedEncodingException("unsupported pto for PatentWeb from google patent: " + info.pto);
            }
//先註解掉solr相關...
//            try {
//                HttpSolrServer solr = PatentDataConfig.patentwebSolr;
//                SolrQuery query = new SolrQuery("url:\"" + url + "\"");
//                QueryResponse res = solr.query(query);
//                List<PatentWeb> pwebs = res.getBeans(PatentWeb.class);
//                if (!pwebs.isEmpty()) {
//                    return pwebs.get(0);
//                }

                String page = WebUtils.downloadPageFromWeb(url);
                if (!page.isEmpty()) {
                    PatentWeb pweb = new PatentWeb();
                    pweb.url = url;
                    pweb.pto_sl = pto.toString();
                    pweb.page_s = page;
                    pweb.provider_sl = "GooglePatent";
                    pweb.do_date = new Date();
                    //solr.addBean(pweb);
                    return pweb;
                }
//            } catch (SolrServerException e) {
//                throw new IOException(e);
//            }

            return null;
        }

//        public static void Test_getPatentWebFromGooglePatent() throws IOException {
//            PatentInfo2 info = PatentInfo2.findOne(Pto.EPO, "52263f0bd4fe4d90490a095c", "pto", "stat", "kindcode", "doDate", "patentNumber", "openNumber", "decisionNumber", "filePageNumber");
//            getPatentWebFromGooglePatent(info);
//        }

        protected static PatentFile downloadFullImageFromGooglePatent(PatentPath patpath, int num) throws IOException {
            PatentFile pfile = new PatentFile();
            pfile.info = patpath.info;

            switch (pfile.info.pto) {
            case US:
            case USPTO:
                break;
            default:
                throw new UnsupportedEncodingException("unsupported pto for download full image from google patent");
            }

            File file = getGooglePatentFile(pfile,
                    Paths.get("fullImage", String.format("%1$d.png", num)));

            String pn = getGooglePatentNumber(pfile.info);
            String url = String.format(
                    "http://patentimages.storage.googleapis.com/pages/%1$s-%2$d.png", pn, num-1);

            if (WebUtils.downloadFileFromWeb(url, file)) {
                pfile.file = file;
                pfile.mimeType = "image/png";
            }

            return pfile;
        }

        protected static PatentFile downloadPDFFromGooglePatent(PatentPath patpath) throws IOException {
            PatentFile pfile = new PatentFile();
            pfile.info = patpath.info;

            String url;
            PatentWeb pweb;
            Pattern pat;
            Matcher mat;
            String pn = getGooglePatentNumber(patpath.info);

            switch (pfile.info.pto) {
            case US:
            case USPTO:
                url = String.format("http://patentimages.storage.googleapis.com/pdfs/%1$s.pdf", pn);
                break;
            case CN:
            case CNIPR:
            case EP:
            case EPO:
                pweb = getPatentWebFromGooglePatent(patpath.info);
                pat = Pattern.compile("(?i)\"appbar-download-pdf-link\" href=\"(\\S*?)\"");
                mat = pat.matcher(pweb.page_s);
                if (mat.find()) {
                    url = mat.group(1);
                    url = url.replaceAll("^//", "http://");
                } else {
                    log.error("no match for download PDF from google patent");
                    return pfile;
                }
                break;
            default:
                throw new UnsupportedEncodingException("unsupported pto for download PDF from google patent");
            }

            File file = getGooglePatentFile(pfile, Paths.get("fullPage.pdf"));

            if (WebUtils.downloadFileFromWeb(url, file)) {
                pfile.file = file;
                pfile.mimeType = "application/pdf";
            }

            return pfile;
        }

        protected static PatentFile downloadFirstImageFromGooglePatent(PatentPath patpath) throws IOException {
            PatentFile pfile = new PatentFile();
            pfile.info = patpath.info;

            String url;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy" + "MM" + "dd");
            String pn = getGooglePatentNumber(patpath.info);

            switch (pfile.info.pto) {
            case US:
            case USPTO:
                url = String.format("http://patentimages.storage.googleapis.com/%1$s%2$s/%1$s%2$s-%3$s-D00000.png", pn, pfile.info.kindcode, dateFormat.format(pfile.info.doDate));
                break;
            default:
                throw new UnsupportedEncodingException("unsupported pto for download PDF from google patent");
            }

            File file = getGooglePatentFile(pfile, Paths.get("firstImage.png"));

            if (WebUtils.downloadFileFromWeb(url, file)) {
                pfile.file = file;
                pfile.mimeType = "application/pdf";
            }

            return pfile;
        }

//        public static void Test_downloadPDFFromGooglePatent() throws IOException {
//            PatentInfo2 info = PatentInfo2.findOne(Pto.USPTO, "522f432a1a970b79995ad6b6", "pto", "stat", "kindcode", "doDate", "patentNumber", "openNumber", "decisionNumber", "filePageNumber");
//            downloadPDFFromGooglePatent(new PatentPath(info));
//        }

    }

    public static PatentFile getFirstPagePdf(PatentPath patpath) throws IOException {
        PatentFile pfile = new PatentFile();
        pfile.info = patpath.info;

        LinkedList<Path> pathlist = FileUtils.listPatentFilesByPrefix(patpath, Paths.get(""), "firstPage.pdf");
        if (pathlist.isEmpty()) {
            LinkedList<Path> pdfPathlist = FileUtils.listPatentFilesByPrefix(patpath, Paths.get(""), "fullPage.pdf");
            if(!pdfPathlist.isEmpty()) {
                for(Path path : pdfPathlist) {
                    pfile.file = path.toFile();
                    pfile.mimeType = "application/pdf";
                    break;
                }
                //US,USPTO 使用PDFBox處理 modify by mike
                switch (pfile.info.pto) {
	                case US:
	                case USPTO:
	                	PDFBoxUtils.splitFirstPagePDF(pfile);
	                	break;
	                case TW:
	                case TIPO:
	                case JP:
	                case JPO:
	                case KR:
	                case KIPO:
	                case EP:
	                case EPO:
	                case WO:
	                case WIPO:
	                	PDFUtils.splitFirstPagePDF(pfile);
	                	break;
	                default:
	                    break;
                }
                pathlist = FileUtils.listPatentFilesByPrefix(patpath, Paths.get(""), "firstPage.pdf");
                for (Path path : pathlist) {
                    pfile.file = path.toFile();
                    pfile.mimeType = "application/pdf";
                    break;
                }
            } else {
                pfile = getFullImage(patpath, 1);
                File cacheFile = getCacheFile(pfile, Paths.get("firstPage.pdf"));
                if(!cacheFile.exists()) {
                    com.lowagie.text.Document doc = null;
                    try {
                        doc = new com.lowagie.text.Document();
                        PdfWriter pdfWriter = PdfWriter.getInstance(doc, new FileOutputStream(cacheFile));
                        doc.open();
                        Rectangle rect = doc.getPageSize();
                        PDFUtils.addImageToPDFbyITEXT(doc, pfile.file.toPath(), rect);
                        doc.close();
                        pdfWriter.close();
                        pfile.file = cacheFile;
                        pfile.mimeType = "application/pdf";
                    } catch (DocumentException e) {
                        cacheFile.delete();
                        throw new IOException(e);
                    } finally {
                        if (doc != null) {
                            doc.close();
                        }
                    }
                }
            }
        } else {
            for (Path path : pathlist) {
                pfile.file = path.toFile();
                pfile.mimeType = "application/pdf";
                break;
            }
        }
        return pfile;
    }

    public static PatentFile getFullImage(PatentPath patpath, int num) throws IOException {
        PatentFile pfile = new PatentFile();
        pfile.info = patpath.info;
        String filenameStart = String.format("%1$d.", num);

        LinkedList<Path> pathlist = FileUtils.listPatentFilesByPrefix(patpath, Paths.get("fullImage"), filenameStart);
        if (pathlist.isEmpty()) {
            switch (pfile.info.pto) {
            case US:
            case USPTO:
            case TW:
            case TIPO:
            case JP:
            case JPO:
            case KR:
            case KIPO:
            case EP:
            case EPO:
            case WO:
            case WIPO:
            case CN:
            case CNIPR:
                pfile = getFullPDF(patpath);
                PDFUtils.splitPDFToImage(pfile, num);
                pfile.file = new File("");
                pfile.mimeType = "";
                pathlist = FileUtils.listPatentFilesByPrefix(patpath, Paths.get("fullImage"), filenameStart);
                for (Path path : pathlist) {
                    pfile.file = path.toFile();
                    pfile.mimeType = Files.probeContentType(path);
                    break;
                }
                break;
            default:
                break;
            }
        } else {
            for (Path path : pathlist) {
                pfile.file = path.toFile();
                pfile.mimeType = Files.probeContentType(path);
                break;
            }
        }

        checkImageMimeType(pfile);
        cachePatentImage2Png(pfile, Paths.get("fullImage", filenameStart + "png"));

        return pfile;
    }

    protected static class FirstImageUtils {

        protected static int[] computeXAxisBlackDotNumber(BufferedImage image, int countThreshold) throws UnsupportedEncodingException {
            ColorModel colorModel = image.getColorModel();
            WritableRaster raster = image.getRaster();
            int width = image.getWidth();
            int height = image.getHeight();
            int[] pixel = { 0, 0, 0, 0 };
            int pixelThreshold = 1;

            int[] xAxisBlockDotNumber = new int[height];
            for (int y = 0; y < height; y++) {
                int sum = 0;
                for (int x = 0; x < width; x++) {
                    raster.getPixel(x, y, pixel);
                    if (pixel[0] >= pixelThreshold) {
                        sum++;
                        if (sum >= countThreshold) {
                            xAxisBlockDotNumber[y] = 1;
                            break;
                        }
                    }
                }
            }

            return xAxisBlockDotNumber;
        }

        protected static int[] computeYAxisBlackDotNumber(BufferedImage image, int countThreshold) throws UnsupportedEncodingException {
            ColorModel colorModel = image.getColorModel();
            WritableRaster raster = image.getRaster();
            int width = image.getWidth();
            int height = image.getHeight();
            int[] pixel = { 0, 0, 0, 0 };
            int pixelThreshold = 1;

            int[] yAxisBlockDotNumber = new int[width];
            for (int x = 0; x < width; x++) {
                int sum = 0;
                for (int y = 0; y < height; y++) {
                    raster.getPixel(x, y, pixel);
                    if (pixel[0] >= pixelThreshold) {
                        sum++;
                        if (sum >= countThreshold) {
                            yAxisBlockDotNumber[x] = 1;
                            break;
                        }
                    }
                }
            }

            return yAxisBlockDotNumber;
        }

        protected static BufferedImage autoCropWhiteImage(BufferedImage image, int countThreshold) throws UnsupportedEncodingException {
            int[] xAxisBlockDotNumber = computeXAxisBlackDotNumber(image, countThreshold);
            Point yCenter = extractNonEmptyCenterRange(xAxisBlockDotNumber);

            int[] yAxisBlockDotNumber = computeYAxisBlackDotNumber(image, countThreshold);
            Point xCenter = extractNonEmptyCenterRange(yAxisBlockDotNumber);

            if (xCenter.x < xCenter.y) {
                if (yCenter.x < yCenter.y) {
                    return image.getSubimage(xCenter.x, yCenter.x, xCenter.y - xCenter.x, yCenter.y - yCenter.x);
                }
            }
            return image;
        }

        /**
         * Point.x: y start Point.y: y end
         */
        protected static LinkedList<Point> extractEmptyRanges(int[] input,
                int start, int end) {
            LinkedList<Point> ranges = new LinkedList<>();
            int len = 0;
            for (int i = start; i < end; i++) {
                if (input[i] > 0) {
                    ranges.add(new Point(i - len, i));
                    len = 0;
                } else {
                    len++;
                }
            }
            if (len > 1) {
                ranges.add(new Point(end - len, end - 1));
            }
            return ranges;
        }

        /**
         * Point.x: y start Point.y: y end
         */
        /* unused
        protected static LinkedList<Point> extractNonEmptyRange(int[] input,
                int start, int minRangeSize, int leadEmptySize) {
            LinkedList<Point> ranges = new LinkedList<>();
            int end = input.length;
            int lead = 0;
            int len = 0;
            start = Math.max(1, start);
            for (int i = start; i < end; i++) {
                if (input[i-1] == input[i]) {
                    // continue block
                    if (input[i] > 0) {
                        len++;
                    } else {
                        lead++;
                    }
                } else {
                    // change dot position
                    if (input[i] > 0) {
                        // white to black
                        len = 1;
                    } else {
                        // black to white
                        if (lead >= leadEmptySize && len > minRangeSize) {
                            ranges.add(new Point(i - len, i));
                        }
                        lead = 1;
                    }
                }
            }
            return ranges;
        }
        //*/

        /**
         * Point.x: y start Point.y: y end
         */
        protected static Point extractNonEmptyCenterRange(int[] input) {
            int total = input.length;
            int start = 0;
            int end = total;
            for (int i = 0; i < total; i++) {
                if (input[i] > 0) {
                    start = i;
                    break;
                }
            }
            for (int i = total - 1; i >= 0; i--) {
                if (input[i] > 0) {
                    end = i;
                    break;
                }
            }
            if (start < end) {
                return new Point(start, end);
            } else {
                return new Point(0, 0);
            }
        }

        /**
         * @return true if generate firstImage.png
         */
        protected static void genFirstImage(PatentFile pfile) throws IOException {
            if (!pfile.file.exists()) {
                return;
            }

            BufferedImage image = ImageIO.read(pfile.file);
            ColorModel colorModel = image.getColorModel();
            int width = image.getWidth();
            int height = image.getHeight();
            int countThreshold = 1;
            int minEmptyHeight = 50;
            int maxEmptyHeight = Math.round(height * 0.15f);
            BufferedImage outimage = null;

            if (colorModel.getPixelSize() != 1) {
                throw new UnsupportedEncodingException("only support 1 bit image");
            }

            // calculate x-axis black dot numbers
            int[] xAxisBlockDotNumber = computeXAxisBlackDotNumber(image, countThreshold);

            if (outimage == null) {
                LinkedList<Point> listEmptyBlock = extractEmptyRanges(xAxisBlockDotNumber,
                        Math.round(height * 0.33f), Math.round(height * 0.95f));
                if (!listEmptyBlock.isEmpty()) {
                    for (Point range : listEmptyBlock) {
                        int rangesize = range.y - range.x;
                        if (rangesize >= minEmptyHeight && rangesize <= maxEmptyHeight) {
                            outimage = image.getSubimage(0, range.x, width, height - range.x - 1);
                            break;
                        }
                    }
                }
            }

            if (outimage == null) {
                outimage = image;
            } else {
                outimage = autoCropWhiteImage(outimage, countThreshold);
            }

            File file = getCacheFile(pfile, Paths.get("firstImage.png"));
            saveImage(outimage, file);
            pfile.file = file;
            pfile.mimeType = "image/png";
        }

    }

    public static PatentFile getFirstImage(PatentPath patpath) throws IOException {
        PatentFile pfile = new PatentFile();
        pfile.info = patpath.info;

        LinkedList<Path> pathlist = FileUtils.listPatentFilesByPrefix(patpath, Paths.get(""), "firstImage.");
        if (pathlist.isEmpty()) {
            switch (pfile.info.pto) {
            case US:
            case USPTO:
                pfile = GooglePatentUtils.downloadFirstImageFromGooglePatent(patpath);
                if (!pfile.file.exists()) {
                    pfile = getFullImage(patpath, 1);
                    FirstImageUtils.genFirstImage(pfile);
                }
                break;
            case CN:
            case CNIPR:
                if (patpath.info.type.equalsIgnoreCase("外观专利")) {
                    pfile = ImageUtils.getFullImage(patpath, 1);
                } else {
                    pfile = CniprPatentUtils.downloadFirstImageFromCnipr(patpath);
                }
                break;
            case TW:
            case TIPO:
            case WO:
            case WIPO:
            case JP:
            case JPO:
                pfile = getFullImage(patpath, 1);
                break;
            default:
                break;
            }
        } else {
            for (Path path : pathlist) {
                pfile.file = path.toFile();
                pfile.mimeType = Files.probeContentType(path);
                break;
            }
        }

        checkImageMimeType(pfile);
        cacheAndResizePatentImage2Png(pfile, Paths.get("firstImage.png"), 250, 250);
        return pfile;
    }

    protected static void genClipImageUS(PatentFile pfile, int num) throws IOException {
        if (!pfile.file.exists()) {
            return;
        }

        BufferedImage image = ImageIO.read(pfile.file);
        ColorModel colorModel = image.getColorModel();
        int width = image.getWidth();
        int height = image.getHeight();
        int emptyThreshold = 0;

        switch (pfile.info.pto) {
        case USPTO:
        case US:
            break;
        default:
            throw new UnsupportedEncodingException("only support US image");
        }

        // trim head title
        BufferedImage trimTitleImage = image.getSubimage(0, 150, width, height - 150);

        BufferedImage imageOut = FirstImageUtils.autoCropWhiteImage(trimTitleImage, emptyThreshold);
        File file = getCacheFile(pfile, Paths.get("clip", num + ".png"));
        saveImage(imageOut, file);
        pfile.file = file;
        pfile.mimeType = "image/png";
    }

    public static PatentFile getClipImage(PatentPath patpath, int num) throws IOException {
        PatentFile pfile = new PatentFile();
        pfile.info = patpath.info;

        String filenameStart = String.format("%1$d.", num);

        LinkedList<Path> pathlist = FileUtils.listPatentFilesByPrefix(patpath, Paths.get("clip"), filenameStart);
        if (pathlist.isEmpty()) {
            switch (pfile.info.pto) {
            case US:
            case USPTO:
                if(patpath.info.filePageFig != null ) {
                    int clipImgAtFullImagePage = num -1 + patpath.info.filePageFig;
                    pfile = getFullImage(patpath, clipImgAtFullImagePage);
                    genClipImageUS(pfile, num);
                }
                break;
            default:
                break;
            }
        } else {
            for (Path path : pathlist) {
                pfile.file = path.toFile();
                pfile.mimeType = Files.probeContentType(path);
                break;
            }
        }
        checkImageMimeType(pfile);
        cachePatentImage2Png(pfile, Paths.get("clip", filenameStart + "png"));

        return pfile;
    }
    
    public static PatentFile getInsetImage(PatentPath patpath, String filename) throws IOException{
        PatentFile pfile = new PatentFile();
        pfile.info = patpath.info;
        String prefix = filename.substring(0, filename.lastIndexOf(".") + 1);
        LinkedList<Path> pathlist = FileUtils.listPatentFilesByPrefix(patpath, Paths.get("figure"), prefix);
        if(!pathlist.isEmpty()){
            pfile.file = pathlist.getFirst().toFile();
            pfile.mimeType = Files.probeContentType(pathlist.getFirst());
        }
        checkImageMimeType(pfile);
        cachePatentImage2Png(pfile, Paths.get("figure", prefix + "png"));
        return pfile;
    }

    protected static PatentFile genFullPDFbyITEXT(PatentPath patpath) throws IOException {
        PatentFile pfile = new PatentFile();
        pfile.info = patpath.info;
//        if (patpath.info.filePageNumber == null) {
//            if (pfile.info.relPatents == null || pfile.info.relPatents.isEmpty()) {
//                return pfile;
//            }
//            PatentInfo2 info = pfile.info.relPatents.get(pfile.info.relPatents.size() - 1);
//            pfile.info = patpath.info = PatentInfo2.findOne(info.pto, info.id, "pto", "stat",
//                    "kindcode", "doDate", "patentNumber", "filePageNumber", "relPatents");
//        }
        switch (patpath.info.pto) {
        case CNIPR:
        case CN:
        case TIPO:
        case TW:
            break;
        default:
            throw new UnsupportedEncodingException("only support TW/CN image to PDF by itext");
        }
        File file = getCacheFile(pfile, Paths.get("fullPage.pdf"));
        FileUtils.sureDirExists(file, true);

        LinkedList<Path> pathlist = FileUtils.listPatentFilesBySuffix(patpath, Paths.get("fullImage"), ".tif", ".jpg");
        if (pathlist.isEmpty()) {
            return pfile;
        }
        com.lowagie.text.Document doc = null;
        try {
            doc = new com.lowagie.text.Document();
            PdfWriter pdfWriter = PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();
            Rectangle rect = doc.getPageSize();
            for (Path path : pathlist) {
                PDFUtils.addImageToPDFbyITEXT(doc, path, rect);
            }
            doc.close();
            pdfWriter.close();
            pfile.file = file;
            pfile.mimeType = "application/pdf";
        } catch (DocumentException e) {
            file.delete();
            throw new IOException(e);
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
        return pfile;
    }

    public static PatentFile getFullPDF(PatentPath patpath) throws IOException {
        PatentFile pfile = new PatentFile();
        pfile.info = patpath.info;
        LinkedList<Path> pathlist = FileUtils.listPatentFilesByPrefix(patpath, Paths.get(""), "fullPage.pdf");
        if (pathlist.isEmpty()) {
            switch (pfile.info.pto) {
            case US:
            case USPTO:
                pfile = GooglePatentUtils.downloadPDFFromGooglePatent(patpath);
                break;
            case CN:
            case CNIPR:
                //pfile = GooglePatentUtils.downloadPDFFromGooglePatent(patpath);
                if (!pfile.file.exists()) {
                    pfile = genFullPDFbyITEXT(patpath);
                }
                break;
            case TW:
            case TIPO:
                if (!pfile.file.exists()) {
                    pfile = genFullPDFbyITEXT(patpath);
                }
                break;
            default:
                break;
            }
        } else {
            for (Path path : pathlist) {
                pfile.file = path.toFile();
                pfile.mimeType = "application/pdf";
                break;
            }
        }

        return pfile;
    }

    public static int getClipCounts(PatentPath patpath) {
        PatentFile pfile = new PatentFile();
        pfile.info = patpath.info;
        LinkedList<Path> pathlist = FileUtils.listPatentFilesBySuffix(patpath, Paths.get("clip"), ".tif", ".jpg", "png");
        if (pathlist.isEmpty()) {
            return 0;
        } else {
            return pathlist.size();
        }
    }
}
