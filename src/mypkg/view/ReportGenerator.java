
package mypkg.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import mypkg.controller.CCAnalyser;

/**
 * Manages report generation for the view
 * @author @author ID:2851383
 */
public class ReportGenerator {
    public ReportGenerator(){
        
    }
         /**
     * generates an html for rendering table data
     *
     * @param map map of table data
     * @param caption table caption
     */
    public void generateReport(HashMap<String, ArrayList> map, String caption) {
        StringBuilder htmlStr = new StringBuilder();
        htmlStr.append("<!DOCTYPE html> ").append("<html> <head>");
        htmlStr.append("<style> table, th, td {border: 1px solid black;border-collapse: collapse;}");
        htmlStr.append("th, td {    padding: 5px;text-align: left;}");
        htmlStr.append("table#t01 {width: 50%;background-color: #f1f1c1;}");
        htmlStr.append("</style> </head>");
        htmlStr.append("<body><table id=\"t01\">");
        htmlStr.append("<caption style=\"text-align:left\"><b>").append(caption).append("</b></caption>");
        ArrayList<String> colHeader = map.get("tableHeader");
        htmlStr.append("<tr>");
        for (String header : colHeader) {
            htmlStr.append("<th>").append(header).append("</th>");
        }
        htmlStr.append("</tr>");
        ArrayList<ArrayList> tableData = map.get("tableData");
        for (ArrayList<String> rowData : tableData) {
            htmlStr.append("<tr>");
            for (String colData : rowData) {
                htmlStr.append("<td>").append(colData).append("</td>");
            }
            htmlStr.append("</tr>");
        }
        htmlStr.append("</table></body> </html>");

        String htmlFile = caption.replaceAll("\\s+", "") + ".html";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(htmlFile)))) {
            writer.write(htmlStr.toString());
        } catch (IOException ex) {
            Logger.getLogger(CCAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
