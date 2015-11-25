package edu.univalle.population;

/**This class assign coordinates to each station**/

import java.util.Collection;
import java.util.Iterator;
import java.io.*;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import edu.univalle.brt.utils.CsvWriter;

public class Generate_Est_Coord {
	private final static Logger log = Logger.getLogger(Generate_Est_Coord.class);
	private static String estacionesShapeFile = "input/shapes/estaciones/mio_estaciones.shp";
	private static String outputFile = "estaciones.csv";

	public static void main(String[] args) {
		
		boolean alreadyExists = new File(outputFile).exists();
		
		try {
			// use FileWriter constructor that specifies open for appending
			CsvWriter csvOutput = new CsvWriter(new FileWriter(outputFile, true), ',');
			
			// if the file didn't already exist then we need to write out the header line
			if (!alreadyExists)
			{
				csvOutput.write("ESTACION");
				csvOutput.write("COORD_X");
				csvOutput.write("COORD_Y");
				csvOutput.endRecord();
			}
			// else assume that the file already has the correct header line
			
			// write out a few records
			@SuppressWarnings("unused")
			int cnt = 0;
			ShapeFileReader shapeFileReader = new ShapeFileReader();
			shapeFileReader.readFileAndInitialize(estacionesShapeFile);
			Collection<SimpleFeature> features = shapeFileReader.getFeatureSet();
			
			Iterator<SimpleFeature> it = features.iterator();
			SimpleFeature ft = null;
			CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:21896");
			log.info("Name of stations:");
			while (it.hasNext()) {
				ft = it.next();
				csvOutput.write(ft.getAttribute("ESTACION").toString());
				Coord coord = CoordUtils.createCoord(Double.parseDouble(ft.getAttribute("GPS_X").toString()), Double.parseDouble(ft.getAttribute("GPS_Y").toString()));
				csvOutput.write(Double.toString(ct.transform(coord).getX()+1));
				csvOutput.write(Double.toString(ct.transform(coord).getY()));
				csvOutput.endRecord();
				cnt++;
			}
						
			csvOutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

}
