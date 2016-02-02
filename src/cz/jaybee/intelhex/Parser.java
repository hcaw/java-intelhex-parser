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
package cz.jaybee.intelhex;

import java.util.List;
import java.util.ArrayList;
import java.io.*;

/**
 * Main Intel HEX parser class
 *
 * @author Jan Breuer
 * @author Kristian Sloth Lauszus
 * @author riilabs
 */
public class Parser {

    private final BufferedReader reader;
    private DataListener dataListener = null;
    private static final int HEX = 16;
    private boolean eof = false;
    private int recordIdx = 0;

    /**
     * Constructor of the parser with reader
     *
     * @param reader
     */
    public Parser(Reader reader) {
        this.reader = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
    }

    /**
     * Constructor of the parser with input stream
     *
     * @param stream
     */
    public Parser(InputStream stream) {
        this.reader = new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * Set data listener to parsing events (data and eof)
     *
     * @param listener
     */
    public void setDataListener(DataListener listener) {
        this.dataListener = listener;
    }

    /**
     * Parse one line of Intel HEX file
     *
     * @param string record
     * @return parsed record
     * @throws IntelHexException
     */
    private Record parseRecord(String record) throws IntelHexException {
        Record result = new Record();
        // check, if there wasn an accidential EOF record
        if (eof) {
            throw new IntelHexException("Data after eof (" + recordIdx + ")");
        }

        // every IntelHEX record must start with ":"
        if (!record.startsWith(":")) {
            throw new IntelHexException("Invalid Intel HEX record (" + recordIdx + ")");
        }

        int lineLength = record.length();
        byte[] hexRecord = new byte[lineLength / 2];

        // sum of all bytes modulo 256 (including checksum) shuld be 0
        int sum = 0;
        for (int i = 0; i < hexRecord.length; i++) {
            String num = record.substring(2 * i + 1, 2 * i + 3);
            hexRecord[i] = (byte) Integer.parseInt(num, HEX);
            sum += hexRecord[i] & 0xff;
        }
        sum &= 0xff;

        if (sum != 0) {
            throw new IntelHexException("Invalid checksum (" + recordIdx + ")");
        }

        // if the length field does not correspond with line length
        int dataLength = hexRecord[0];
        if ((dataLength + 5) != hexRecord.length) {
            throw new IntelHexException("Invalid record length (" + recordIdx + ")");
        }

        // determine record type
        result.type = RecordType.fromInt(hexRecord[3] & 0xFF);
        if (result.type == RecordType.UNKNOWN) {
            throw new IntelHexException("Unsupported record type " + (hexRecord[3] & 0xFF) + " (" + recordIdx + ")");
        }

        List<Byte> tempArrayList = new ArrayList<Byte>();
        Byte dle = 0x10;
        Byte soh = 0x01;
        Byte eot = 0x04;
        for(int i = 0;i<hexRecord.length;i++) {
            if(hexRecord[i] == 0x01 || hexRecord[i] == 0x04 || hexRecord[i] == 0x10) {
                tempArrayList.add(dle); //CHECK SYNTAX
            }
            tempArrayList.add(hexRecord[i]);
        }
        tempArrayList.add(0,soh); //Add <SOH>
        tempArrayList.add(eot); //Add <EOT>

        Byte[] newHexRecord = new Byte[tempArrayList.size()];
        newHexRecord = tempArrayList.toArray(newHexRecord);

        result.contents = newHexRecord;

        return result;
    }

    /**
     * Process parsed record, copute correct address, emit events
     *
     * @param record
     */
    private void processRecord(Record record) {
        dataListener.data(record.contents);
        if(record.type == EOF) {
            dataListener.eof();
            eof = true;
        }
    }

    /**
     * Main public method to start parsing of the input
     *
     * @throws IntelHexException
     * @throws IOException
     */
    public void parse() throws IntelHexException, IOException {
        eof = false;
        recordIdx = 1;
        String recordStr;

        while ((recordStr = reader.readLine()) != null) {
            Record record = parseRecord(recordStr);
            processRecord(record);
            recordIdx++;
        }

        if (!eof) {
            throw new IntelHexException("No eof at the end of file");
        }
    }
}
