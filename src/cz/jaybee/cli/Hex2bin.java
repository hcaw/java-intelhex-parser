/**
 * Copyright (c) 2015, Jan Breuer All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package cz.jaybee.cli;

import cz.jaybee.intelhex.IntelHexException;
import cz.jaybee.intelhex.Parser;
import cz.jaybee.intelhex.listeners.BinWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to demonstrate usage of Intel HEX parser
 *
 * @author Jan Breuer
 */
public class Hex2bin {

    /**
     * Convert Intel HEX to bin
     *
     * usage:
     *
     * Hex2bin {source} {target}
     *
     * {source} is source Intel HEX file name
     *
     * {target} is target BIN file name
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String fileIn = "";
        String fileOut = "";

        if (args.length != 2) {
            System.out.println("usage:");
            System.out.println("    hex2bin <hex> <bin> ");
            System.out.println();
            System.out.println("    full address range of app.hex");
            System.out.println("        hex2bin app.hex app.bin");
            return;
        } else {
            fileIn = args[0];
            fileOut = args[1];
        }

        try (FileInputStream is = new FileInputStream(fileIn)) {
            OutputStream os = new FileOutputStream(fileOut);
            Parser parser = new Parser(is);
            BinWriter writer = new BinWriter(os);
            parser.setDataListener(writer);
            parser.parse();

        } catch (IntelHexException | IOException ex) {
            Logger.getLogger(Hex2bin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}