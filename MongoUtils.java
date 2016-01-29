package itec.utils;

import itec.patent.common.DateUtils;
import itec.patent.mongodb.PatentInfo2;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tsaikd.java.mongodb.QueryHelp;

public class MongoUtils {
    
    static Log log = LogFactory.getLog(MongoUtils.class);
    
    public static HashMap<String, String> ptoMap = new HashMap<String, String>() {
        private static final long serialVersionUID = 4771172329483959359L;
        {
            put("US", "US");
            put("USPTO", "US");
            put("CN", "CN");
            put("CNIPR", "CN");
            put("JP", "JP");
            put("JPO", "JP");
            put("EP", "EP");
            put("EPO", "EP");
            put("WO", "WO");
            put("WIPO", "WO");
            put("KR", "KR");
            put("KIPO", "KR");
        }
    };
    
    public static String getRelPatentPath(PatentInfo2 info) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return String.format("%s%d%s/%s/%s"
            , ptoMap.get(info.pto.toString()).toLowerCase()
            , info.stat
            , info.kindcode == null ? "" : info.kindcode.toLowerCase()
            , sdf.format(info.doDate)
            , info.patentNumber.replaceAll("[\\/\\s]", "").toLowerCase()
        );
    }
    
    public static QueryHelp getDateRange(String dateRange) {
        if (dateRange == null || dateRange.isEmpty()) {
            return null;
        }
        Date dateFrom = null;
        Date dateTo = null;

        if (dateRange.contains("-")) {
            String[] tParts = dateRange.split("-");
            dateFrom = DateUtils.parseDate(tParts[0]);
            dateTo = DateUtils.parseDate(tParts[1]);
        } else if (dateRange.contains("+")) {
            int caltype;
            String[] tParts = dateRange.split("\\+");
            switch (tParts[0].length()) {
            case 4:
                caltype = Calendar.YEAR;
                tParts[0] += "0101";
                break;
            case 6:
                caltype = Calendar.MONTH;
                tParts[0] += "01";
                break;
            case 8:
                caltype = Calendar.DATE;
                break;
            default:
                throw new IllegalArgumentException("Invalid date format");
            }
            dateFrom = DateUtils.parseDate(tParts[0]);
            Calendar calFrom = Calendar.getInstance();
            calFrom.setTime(dateFrom);

            Calendar calTo = Calendar.getInstance();
            calTo.setTime(dateFrom);
            calTo.set(caltype, calFrom.get(caltype) + Integer.parseInt(tParts[1]));
            dateTo = new Date(calTo.getTimeInMillis());
        }
        QueryHelp doDate = new QueryHelp();
        doDate.filter("$gte", dateFrom);
        doDate.filter("$lt", dateTo);
        return new QueryHelp("doDate", doDate);
    }
    
    public static QueryHelp getDateRange(String startDate, String endDate) {
        if (startDate == null || startDate.isEmpty() || endDate == null || endDate.isEmpty()) {
            return null;
        }
        Date dateFrom = DateUtils.parseDate(startDate);
        Date dateTo = DateUtils.parseDate(endDate);
        QueryHelp doDate = new QueryHelp();
        doDate.filter("$gte", dateFrom);
        doDate.filter("$lt", dateTo);
        return new QueryHelp("doDate", doDate);
    }
}
