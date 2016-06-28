package edu.univalle.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CodeToName
{
    /*Take files from stats/base/ which contains volumes with stations codes and put them into
     * stats/volumes, transforming station codes into stations names*/

    public static void main(String[] args) {
        String source_folder = "C:/MATSim/workspace_mars/dta-brt/input/stats/base/";
        String final_folder = "C:/MATSim/workspace_mars/dta-brt/input/stats/volumes/";
        List<String> file_names = new ArrayList<String>();

        HashMap<Integer, String> stations_code = Map_Codes.map_codes("input/std_code.csv");
        file_names.add("Volumes-E21_Ida_6-8.csv");
        file_names.add("Volumes-E21_Vuelta_6-8.csv");
        file_names.add("Volumes-E31_Ida_6-8.csv");
        file_names.add("Volumes-E31_Vuelta_6-8.csv");
        file_names.add("Volumes-T31_Ida_6-8.csv");
        file_names.add("Volumes-T31_Vuelta_6-8.csv");
        file_names.add("Volumes-E21_Ida_12-14.csv");
        file_names.add("Volumes-E21_Vuelta_12-14.csv");
        file_names.add("Volumes-E31_Ida_12-14.csv");
        file_names.add("Volumes-E31_Vuelta_12-14.csv");
        file_names.add("Volumes-T31_Ida_12-14.csv");
        file_names.add("Volumes-T31_Vuelta_12-14.csv");
        file_names.add("Volumes-E21_Ida_17-19.csv");
        file_names.add("Volumes-E21_Vuelta_17-19.csv");
        file_names.add("Volumes-E31_Ida_17-19.csv");
        file_names.add("Volumes-E31_Vuelta_17-19.csv");
        file_names.add("Volumes-T31_Ida_17-19.csv");
        file_names.add("Volumes-T31_Vuelta_17-19.csv");

        for (int i = 0; i < file_names.size(); i++) {
            String source_file = source_folder + file_names.get(i);
            String final_file = final_folder + file_names.get(i);
            // System.out.println(final_file);

            boolean alreadyExists_coord = new File(final_file).exists();
            try {
                CsvWriter new_file = new CsvWriter(new FileWriter(final_file, true), ',');
                if (!alreadyExists_coord) {
                    new_file.write("id");
                    new_file.write("entering");
                    new_file.write("leaving");
                    new_file.write("passthrough");
                    new_file.write("totalVolume");
                    new_file.endRecord();

                    try {
                        CsvReader old_file = new CsvReader(source_file);
                        old_file.readHeaders();

                        while (old_file.readRecord()) {

                            String id = old_file.get("id");
                            String entering = old_file.get("entering");
                            String leaving = old_file.get("leaving");
                            String passing = old_file.get("passthrough");
                            String total = old_file.get("totalVolume");

                            new_file.write(stations_code.get(Integer.parseInt(id)));
                            new_file.write(entering);
                            new_file.write(leaving);
                            new_file.write(passing);
                            new_file.write(total);
                            new_file.endRecord();

                        }

                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    new_file.close();

                }
                else {
                    System.out.println("File already exsists");
                }

            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
