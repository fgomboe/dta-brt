package edu.univalle.population;

import java.io.IOException;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.utils.io.UncheckedIOException;

public class HeadXml extends ConfigWriter
{
    String input;
    String output;

    public HeadXml(Config config, String input, String output) {
        super(config);
        this.input = input;
        this.output = output;
        // TODO Auto-generated constructor stub
    }

    protected void writeDoctype(String rootTag, String dtdUrl) throws UncheckedIOException {
        try {
            this.writer
                    .write("<!DOCTYPE " + rootTag + " SYSTEM \"" + dtdUrl + "\"\n" + "\t[\n" + "\t\t<!ENTITY OUTBASE \""
                            + output + "\">\n" + "\t\t<!ENTITY INBASE  \"" + input + "\">\n" + "\t]\n>\n");
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
