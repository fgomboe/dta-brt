package edu.univalle.population;

import java.io.*;

import edu.univalle.brt.utils.CsvReader;
import edu.univalle.brt.utils.CsvWriter;

/**
 * This class extract useful data from file Usos_Detallados 01-04-2014.csv"
 *
 */
public class Extract_Usos 
{
	private static String outputFile = "usos.csv";
    
	public static void main( String[] args )
    {
    	
    	try {
			
			CsvReader products = new CsvReader("input/Usos_Detallados 01-04-2014.csv");
		
			products.readHeaders();
			System.out.println("ID_PAS - ID_ESTACION - N_ESTACION - HORA");
			
			boolean alreadyExists = new File(outputFile).exists();
			
			try {
				// use FileWriter constructor that specifies open for appending
				CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
				
				// if the file didn't already exist then we need to write out the header line
				if (!alreadyExists)
				{
					csvOutput.write("ID_PAS");
					csvOutput.write("ID_ESTACION");
					csvOutput.write("N_ESTACION");
					csvOutput.write("HORA");
					csvOutput.endRecord();
				}
				// else assume that the file already has the correct header line
				while (products.readRecord())
				{
					
					String id_passenger = products.get("CRD_SNR");
					String id_estacion = products.get("VEH_ID");
					String name_estacion = products.get("ESTACION");
					String time_arrival = products.get("HORA");
					
					
					
					if (Integer.parseInt(id_estacion)<10000){ // Saca solo los registros de estaciones
						
						csvOutput.write(id_passenger);
						csvOutput.write(id_estacion);
						csvOutput.write(name_estacion);
						csvOutput.write(time_arrival);
						csvOutput.endRecord();
					}
					// perform program logic here
					
				}
		
				// write out a few records
				
							
				csvOutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			
			
			products.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
