/*
 *
 */
package com.dua3.meja.converter;

import java.io.File;
import java.io.IOException;

import com.dua3.meja.model.Workbook;
import com.dua3.meja.model.generic.GenericWorkbookFactory;

/**
 * Converter sample application.
 * <p>
 * Demonstrates reading and writing workbooks. The relevant format of the input
 * and output filenames is determined by the file extension.
 * </p>
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class Converter {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.format("Usage: %s <input_file> <output_file>%n", Converter.class.getName());
            System.exit(1);
        }

        File in = new File(args[0]);
        File out = new File(args[1]);

        try (Workbook workbook = GenericWorkbookFactory.instance().open(in)) {
            workbook.write(out, false);
            System.out.println("Data written to " + out.getAbsolutePath());
        } catch (IOException ex) {
            System.err.println("I/O Error: " + ex.getMessage());
        }
    }

}
