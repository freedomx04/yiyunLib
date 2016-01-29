package itec.utils;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PageUtils {
    
    static Log log = LogFactory.getLog(PageUtils.class);
    
    public static List<String> cssFiles = new LinkedList<String>();

    public static List<String> jsFiles = new LinkedList<String>();
    
    static {
        String dir;
        // main css
        cssFiles.add("css/main.css");
        cssFiles.add("css/ui-icon.css");
        
        // base js
        jsFiles.add("js/base/base.js");
        
        // date
        jsFiles.add("js/date/date.format-1.2.3.js");
        jsFiles.add("js/date/date.js");
        
        // jquery
        jsFiles.add("js/jquery/1.10.1/jquery.js");
        jsFiles.add("js/jquery/jquery-doT.js");
        
        // dataTables
        jsFiles.add("js/dataTables/js/jquery.dataTables.js");
        cssFiles.add("js/dataTables/css/jquery.dataTables.css");
        
        // doT
        jsFiles.add("js/doT/1.0.1/doT.js");
        
        // jquery ui files
        dir = "js/jquery-ui/1.10.2/";
        cssFiles.add(dir + "jquery.ui.core.css");
        jsFiles.add(dir + "jquery.ui.core.js");
        jsFiles.add(dir + "jquery.ui.widget.js");
        jsFiles.add(dir + "jquery.ui.position.js");
        jsFiles.add(dir + "jquery.ui.mouse.js");

        jsFiles.add(dir + "jquery.ui.draggable.js");
        jsFiles.add(dir + "jquery.ui.droppable.js");
        cssFiles.add(dir + "jquery.ui.resizable.css");
        jsFiles.add(dir + "jquery.ui.resizable.js");

        cssFiles.add(dir + "jquery.ui.menu.css");
        jsFiles.add(dir + "jquery.ui.menu.js");
        cssFiles.add(dir + "jquery.ui.autocomplete.css");
        cssFiles.add(dir + "jquery.ui.autocomplete.fix.css");
        jsFiles.add(dir + "jquery.ui.autocomplete.js");

        cssFiles.add(dir + "jquery.ui.button.css");
        jsFiles.add(dir + "jquery.ui.button.js");
        cssFiles.add(dir + "jquery.ui.dialog.css");
        cssFiles.add(dir + "jquery.ui.dialog.fix.css");
        jsFiles.add(dir + "jquery.ui.dialog.js");
        cssFiles.add(dir + "jquery.ui.progressbar.css");
        jsFiles.add(dir + "jquery.ui.progressbar.js");
        cssFiles.add(dir + "jquery.ui.slider.css");
        jsFiles.add(dir + "jquery.ui.slider.js");
        
        cssFiles.add(dir + "jquery.ui.datepicker.css");
        jsFiles.add(dir + "jquery.ui.datepicker.js");
        
    }

}
