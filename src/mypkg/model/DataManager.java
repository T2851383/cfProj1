
package mypkg.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import mypkg.controller.CCAnalyser;

/**
 * Manages data loading and retrieval
 * @author @author ID:2851383
 */
public class DataManager {
        public static final String ZCTA_EXTRACT = "Seq15_zcta_extract.csv";
    public static final String COMPLAINTS_EXTRACT = "Mortgage_Complaints_extract.csv";
    public DataManager(){
        
    }
   /**
     * Loads Census ACS ZCTA Extract ( a subset of the actual extract after
     * merging data from summary level and geo data sources and discarding
     * unused columns)
     *
     * @param conn DB connection
     *
     */
    public void loadZcta(Connection conn) {
        File f = new File(ZCTA_EXTRACT);
        // try-with-resources ensures resources are closed after use 
        try (Scanner scanner = new Scanner(f); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("drop table if exists zcta");
            stmt.executeUpdate("create table zcta(recordID NUMBER,zip TEXT, pop NUMBER,income NUMBER)");
            PreparedStatement pstmt = conn.prepareStatement("insert into zcta values(?,?,?,?)");
            // Turn off to ensure all or none transaction
            conn.setAutoCommit(false);
            String line = scanner.nextLine();
            while (scanner.hasNext()) {
                line = scanner.nextLine();
                String[] fields = line.split(",");

                pstmt.setInt(1, new Integer(fields[0]));
                pstmt.setString(2, fields[1].substring(7));
                int pop = Character.isDigit(fields[2].charAt(0)) ? new Integer(fields[2]) : 0;
                pstmt.setInt(3, pop);
                int income = Character.isDigit(fields[3].charAt(0)) ? new Integer(fields[3]) : 0;
                pstmt.setInt(4, income);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (FileNotFoundException | SQLException ex) {
            Logger.getLogger(CCAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Loads consumer complaints extract ( a subset of mortgage complaints data
     * source)
     *
     * @param conn DB connection
     */
    public void loadComplaints(Connection conn) {
        File f = new File(COMPLAINTS_EXTRACT);
        try (Scanner scanner = new Scanner(f); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("drop table if exists mortcomp");
            stmt.executeUpdate("create table mortcomp(date TEXT,state TEXT, zip TEXT, compid NUMBER)");
            PreparedStatement pstmt = conn.prepareStatement("insert into mortcomp values(?,?,?,?)");
            conn.setAutoCommit(false);
            String line = scanner.nextLine();
            while (scanner.hasNext()) {
                line = scanner.nextLine();
                String[] fields = line.split(",");
                String[] dateFields = fields[0].split("/");
                String date = "";
                // format date if exists
                if (dateFields.length > 2) {
                    String month = String.format("%02d", new Integer(dateFields[0]));
                    String day = String.format("%02d", new Integer(dateFields[1]));
                    String year = dateFields[2];
                    date = year + month + day;
                }
                // add leading zeros to zipcode where needed to ensure a 5-digit code
                String zip = "";
                if (fields[2].length() > 0) {
                    zip = ("00000" + fields[2]).substring(fields[2].length());
                }

                pstmt.setString(1, date);
                pstmt.setString(2, fields[1]);
                pstmt.setString(3, zip);
                pstmt.setInt(4, new Integer(fields[3]));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (FileNotFoundException | SQLException ex) {
            Logger.getLogger(CCAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * conducts analysis to establish a correlation between population/income
     * and consumer complaints aggregated by zip code. For regional analysis,
     * use the first digit of the zip code.
     *
     * @param conn DB connection
     * @param zipStr a wildcard prefix provided for zip code
     * @param isIncome true=income aggregate; false=pop aggregate
     * @return a map of table data
     */
    public HashMap anlayseByZip(Connection conn, String zipStr, boolean isIncome) {
        HashMap<String, ArrayList> map = new HashMap<>();

        try {
            String query = null;
            ArrayList<String> colLabels = new ArrayList<>();
            ArrayList<ArrayList> tableData = new ArrayList<>();

            if (isIncome) {
                query = "select count(m.zip), c.income from "
                        + "(select avg(income) income from zcta z where z.zip like ?)c,"
                        + " mortcomp m where m.zip like ?";
                colLabels.add("Num_Complaints");
                colLabels.add("Avg_income");
            } else {
                colLabels.add("Num_Complaints");
                colLabels.add("Population");

                query = "select count(m.zip), c.population from "
                        + "(select sum(pop) population from zcta z where z.zip like ?)c,"
                        + " mortcomp m where m.zip like ?";

            }
            map.put("tableHeader", colLabels);

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, zipStr);
            pstmt.setString(2, zipStr);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ArrayList<String> colData = new ArrayList<>();
                colData.add(Integer.toString(rs.getInt(1)));
                colData.add(Integer.toString(rs.getInt(2)));
                tableData.add(colData);

            }
            map.put("tableData", tableData);

        } catch (SQLException ex) {
            Logger.getLogger(CCAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }

    /**
     * Provides a summary report by state or year
     *
     * @param conn DB connection
     * @param isState true=state wide summary; false=yearly summary
     * @return a map of table data
     */
    public HashMap anlayseBySY(Connection conn, boolean isState) {
        HashMap<String, ArrayList> map = new HashMap<>();

        try {
            String query = null;
            ArrayList<String> colLabels = new ArrayList<>();
            ArrayList<ArrayList> tableData = new ArrayList<>();
            if (isState) {
                query = "select state, count(compid) from "
                        + " mortcomp group by state order by state";
                colLabels.add("State");
            } else {
                query = "select substr(date,1,4) year, count(compid) from  "
                        + " mortcomp group by year order by year";
                colLabels.add("Year");
            }
            colLabels.add("#of Compliants");
            map.put("tableHeader", colLabels);

            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ArrayList<String> colData = new ArrayList<>();
                colData.add(rs.getString(1));
                colData.add(Integer.toString(rs.getInt(2)));
                tableData.add(colData);

            }
            map.put("tableData", tableData);

        } catch (SQLException ex) {
            Logger.getLogger(CCAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return map;
    }


    
}
