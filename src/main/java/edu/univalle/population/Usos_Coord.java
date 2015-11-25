package edu.univalle.population;

/**This class assign coordinates to registers of trips by searching in files generated
 * with Extract_Usos.java and Generate_Est_Coord.java**/

import java.io.*;

import edu.univalle.brt.utils.CsvReader;
import edu.univalle.brt.utils.CsvWriter;

public class Usos_Coord 
{
	//declare name for output file
	private static String outputFile_coord = "usos_coord.csv";

	public static void main( String[] args )
	{

		try {
			//Open file with smart card data
			CsvReader matriz_usos = new CsvReader("input/Usos_Detallados 01-04-2014.csv");
			System.out.println("### extracting data from "+matriz_usos.toString()+" ###");

			//Read file header
			matriz_usos.readHeaders();
			boolean alreadyExists_coord = new File(outputFile_coord).exists();

			try {
				// use FileWriter constructor that specifies open for appending
				//Create output file
				CsvWriter csvOutput_coord = new CsvWriter(new FileWriter(outputFile_coord, true), ',');

				// if the file didn't already exist then we need to write out the header line
				if (!alreadyExists_coord)
				{
					csvOutput_coord.write("ID_PAS");
					csvOutput_coord.write("ID_ESTACION");
					csvOutput_coord.write("N_ESTACION");
					csvOutput_coord.write("HORA");
					csvOutput_coord.write("COORD_X");
					csvOutput_coord.write("COORD_Y");
					csvOutput_coord.endRecord();
				}
				// else assume that the file already has the correct header line
				//we sweep the file with smart card data
				while (matriz_usos.readRecord())

				{
					String id_estacion = matriz_usos.get("VEH_ID");
					//Specification says that veh_id<1000 correspond to stations, otherwise is a secondary bus
					if (Integer.parseInt(id_estacion)<10000){ // Saca solo los registros de estaciones

						//If we got an station input, we extract data

						String id_passenger = matriz_usos.get("CRD_SNR");
						String name_estacion = matriz_usos.get("ESTACION");
						String time_arrival = matriz_usos.get("HORA");

						csvOutput_coord.write(id_passenger);
						csvOutput_coord.write(id_estacion);
						csvOutput_coord.write(name_estacion);
						csvOutput_coord.write(time_arrival);

						try {

							/*for each "station" register, we search in "estaciones_matrizUsos.csv"
							 * which contains coordenates in adequate system for each station
							 * according to its name
							 */

							CsvReader matriz_usos_coord = new CsvReader("input/estaciones_matrizUsos.csv");
							matriz_usos_coord.readHeaders();
							while (matriz_usos_coord.readRecord())
							{
								String name_estacion_coord = matriz_usos_coord.get("MATRIZ USOS");
								if(name_estacion.equals(name_estacion_coord)){
									//when we find the right station name, we extract its coordenates
									String coord_x = matriz_usos_coord.get("COORD_X");
									String coord_y = matriz_usos_coord.get("COORD_Y");
									csvOutput_coord.write(coord_x);
									csvOutput_coord.write(coord_y);
									break;
								}

							}
							matriz_usos_coord.close();
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						csvOutput_coord.endRecord();
					}
				}

				csvOutput_coord.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			matriz_usos.close();
			System.out.println("### File construction finished #####");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
