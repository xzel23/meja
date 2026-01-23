/*
 *
 */
package com.dua3.meja.converter;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;
import org.slb4j.SLB4J;

import java.io.File;
import java.io.IOException;

/**
 * Converter sample application.
 * <p>
 * Demonstrates reading and writing workbooks. The file extension determines the
 * relevant format of the input and output files.
 */
public final class Converter {

    static {
        SLB4J.init();
    }

    private Converter() {
    }

    /**
     * The main method is the entry point of the program.
     *
     * @param args an array of strings representing the command line arguments.
     *             The first argument should be the path of the input file,
     *             and the second argument should be the path of the output file.
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.format("Usage: %s <input_file> <output_file>%n", Converter.class.getName());
            System.exit(1);
        }

        File in = new File(args[0]);
        File out = new File(args[1]);

        if (out.exists()) {
            System.err.println("Outfile already exists.");
        }

        try (Workbook workbook = GenericWorkbookFactory.instance().open(in.toURI())) {
            workbook.write(out.toURI());
            System.out.println("Data written to " + out.getAbsolutePath());
        } catch (IOException ex) {
            System.err.println("IO error: " + ex.getMessage());
        }
    }

}
