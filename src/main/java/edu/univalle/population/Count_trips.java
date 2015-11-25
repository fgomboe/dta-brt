package edu.univalle.population;

/**This class counts and filters one-trip registers optionally**/

import java.util.*;
import java.io.*;

import edu.univalle.utils.CsvReader;
import edu.univalle.utils.CsvWriter;

public class Count_trips {

	private static String[] id_list=new String[314748];
	private static String outputFile_trips = "number_trips.csv";
	private static String outputFile_trips_reg = "number_trips_ord.csv";
	public static void main(String[] args) {
		// TODO Auto-generated method stub


		try{

			CsvReader usos_coord = new CsvReader("usos_coord.csv");
			System.out.println("### extracting data from "+usos_coord.toString()+" ###");
			usos_coord.readHeaders();

			int id_index=0;

			while (usos_coord.readRecord())

			{
				id_list[id_index]=usos_coord.get("ID_PAS");
				id_index++;
			}
			usos_coord.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean alreadyExists_coord = new File(outputFile_trips).exists();
		try {
			// use FileWriter constructor that specifies open for appending
			//Create output file
			CsvWriter csvOutput_coord = new CsvWriter(new FileWriter(outputFile_trips), ',');

			if (!alreadyExists_coord)
			{
				csvOutput_coord.write("ID_PAS");
				csvOutput_coord.write("# of TRIPS");
				csvOutput_coord.endRecord();
			}

			boolean alreadyExists_ord = new File(outputFile_trips_reg).exists();
			try {
				// use FileWriter constructor that specifies open for appending
				//Create output file
				CsvWriter csvOutput_ord = new CsvWriter(new FileWriter(outputFile_trips_reg), ',');

				if (!alreadyExists_ord)
				{
					csvOutput_ord.write("ID_PAS");
					csvOutput_ord.write("ID_ESTACION");
					csvOutput_ord.write("N_ESTACION");
					csvOutput_ord.write("HORA");
					csvOutput_ord.write("COORD_X");
					csvOutput_ord.write("COORD_Y");
					csvOutput_ord.endRecord();
				}

				//Counts repetitions of each value
				HashMap<Integer, Integer> repetitions = new HashMap<Integer, Integer>();

				for (int i = 0; i < id_list.length; ++i) {
					int item = Integer.parseInt(id_list[i]);

					if (repetitions.containsKey(item)){
						//System.out.println(repetitions);
						//System.out.println(item);
						repetitions.put(item, repetitions.get(item) + 1);
					}

					else
						repetitions.put(item, 1);
				}

				@SuppressWarnings("unused")
				int overAllCount = 0;

				for (Map.Entry<Integer, Integer > e : repetitions.entrySet()) {
					//for (Map.Entry<Integer, Integer> e : repetitions.entrySet()) {
					/**If you want to filter 1-trip registers, you can add
					 *  if (e.getValue() > 1) {} wrapping the next 4 lines**/
					if (e.getValue() > 1) {
						overAllCount += 1;
						csvOutput_coord.write(e.getKey().toString());
						csvOutput_coord.write(e.getValue().toString());
						csvOutput_coord.endRecord();
						System.out.println("new line");









												try{

													CsvReader usos_coord1 = new CsvReader("usos_coord.csv");
													System.out.println("### extracting data from "+usos_coord1.toString()+" ###");
													usos_coord1.readHeaders();
						//
						//
													while (usos_coord1.readRecord())
						
													{
														//System.out.println(e.getKey().toString() + "-" +usos_coord1.get("ID_PAS").toString());
						//								//String x= e.getKey().toString();
						//								//usos_coord.get("ID_PAS");
														if(usos_coord1.get("ID_PAS").toString().equals(e.getKey().toString())){
						//create data ordered and filtered
															System.out.println(usos_coord1.getRawRecord());
															break;
						//									csvOutput_ord.write(usos_coord.get("ID_PAS"));
						//									csvOutput_ord.write(usos_coord.get("ID_ESTACION"));
						//									csvOutput_ord.write(usos_coord.get("N_ESTACION"));
						//									csvOutput_ord.write(usos_coord.get("HORA"));
						//									csvOutput_ord.write(usos_coord.get("COORD_X"));
						//									csvOutput_ord.write(usos_coord.get("COORD_Y"));
						//									csvOutput_ord.endRecord();
														}
														
														
													}
						//
						//
													usos_coord1.close();
												} catch (FileNotFoundException o) {
													o.printStackTrace();
												} catch (IOException o) {
													o.printStackTrace();
												}








					}


				}





				csvOutput_ord.close();
			} catch (IOException a) {
				a.printStackTrace();
			}


			csvOutput_coord.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}


