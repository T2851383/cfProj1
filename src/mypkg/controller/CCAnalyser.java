
package mypkg.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import mypkg.model.DataManager;
import mypkg.view.ReportGenerator;

/**
 * Interacts with model for DB transactions/retrievals and view for
 * report generation
 * @author ID:2851383
 */
public class CCAnalyser {


    public CCAnalyser() {

    }

    public static void main(String[] args) {
        //open a connection to sqlite and close it after done
        String summaryType = System.getProperty("summary", "none");
        String caption = "";
        HashMap map = null;
        DataManager dm = new DataManager();
        ReportGenerator rg = new ReportGenerator();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:sample.db")) {
            // load zcta and complaints data extracts
            dm.loadZcta(conn);
            dm.loadComplaints(conn);
            //perform data analysis and generate reports based on user input
            if (summaryType.equalsIgnoreCase("state") || 
                    summaryType.equals("none")) {
                map = dm.anlayseBySY(conn, true);
                caption = "Summary by State";
            } else if (summaryType.equalsIgnoreCase("yearly")) {
                map = dm.anlayseBySY(conn, false);
                caption = "Yearly Summary";

            } else if (summaryType.equalsIgnoreCase("zipin")
                    || summaryType.equalsIgnoreCase("zipop")) {
                String zipPrefix = System.getProperty("zip", "0");
                String zipPrefixStr = zipPrefix + "%";
                if (summaryType.equalsIgnoreCase("zipin")) {
                    map = dm.anlayseByZip(conn, zipPrefixStr, true);
                    caption = "Income Summary by ZipPrefix "+ zipPrefix;
                } else {
                    map = dm.anlayseByZip(conn, zipPrefixStr, false);
                    caption = "Population Summary by ZipPrefix "+zipPrefix;

                }
            }
            // generate selected report in html format
            rg.generateReport(map, caption);
        } catch (SQLException ex) {
            Logger.getLogger(CCAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
