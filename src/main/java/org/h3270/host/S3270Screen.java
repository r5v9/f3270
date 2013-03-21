package org.h3270.host;

/*
 * Copyright (C) 2003-2006 akquinet framework solutions
 *
 * This file is part of h3270.
 *
 * h3270 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * h3270 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with h3270; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, 
 * MA 02110-1301 USA
 */

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of the Screen interface that is fed by the output of s3270.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id: S3270Screen.java,v 1.21 2006/10/25 11:20:09 spiegel Exp $
 */
public class S3270Screen extends AbstractScreen {

    private List<String> bufferData = null;
    private String status = null;

    public S3270Screen() {
        width = 0;
        height = 0;
        buffer = null;
        isFormatted = true;
    }

    public S3270Screen(final InputStream in) {
        try {
            final BufferedReader input = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
            final List<String> lines = new ArrayList<String>();
            String status = null;
            while (true) {
                final String line = input.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("data:")) {
                    lines.add(line);
                } else if (Pattern.matches("[ULE] [UF] [UC] .*", line)) {
                    status = line;
                }
            }
            update(status, lines);
        } catch (final IOException ex) {
            throw new RuntimeException("error: " + ex);
        }
    }

    /**
     * Pattern that matches a status line from s3270. Example: U F U C(hostname) I 3 32 80 22 15 0x0 -
     */
    private static Pattern statusPattern = Pattern.compile("^[ULE] " // Keyboard State
            + "[FU] " // Formatted / Unformatted
            + "[PU] " // Protected / Unprotected (at cursor)
            + "(?:C\\([^)]*\\)|N) " // Connected / Not Connected
            + "[ILCN] " // Emulator Mode
            + "[2-5] " // Model Number
            + "[0-9]+ " // Number of Rows
            + "[0-9]+ " // Number of Columns
            + "([0-9]+) " // Cursor Row
            + "([0-9]+) " // Cursor Column
            + "0x0 " // Window ID (always 0x0)
            + "(?:[0-9.]+|-)$" // Time for last command
    );

    /**
     * Updates this screen with output from "readbuffer ascii".
     * 
     * @param status
     *            the status line that was returned by s3270
     * @param bufferData
     *            the actual screen data, as a list of strings
     */
    public void update(final String status, final List<String> bufferData) {
        this.status = status;
        if (status.charAt(2) == 'F') {
            isFormatted = true;
            updateBuffer(bufferData);
        } else {
            isFormatted = false;
            updateBuffer(bufferData);
        }
        final Matcher m = statusPattern.matcher(status);
        if (m.find()) {
            cursorX = Integer.parseInt(m.group(2));
            cursorY = Integer.parseInt(m.group(1));
            final InputField f = getInputFieldAt(cursorX, cursorY);
            if (f != null) {
                f.setFocused(true);
            }
        } else {
            cursorX = 0;
            cursorY = 0;
        }
    }

    private void updateBuffer(final List<String> bufferData) {
        this.bufferData = new ArrayList<String>(bufferData);
        height = bufferData.size();
        width = 0;
        buffer = new char[height][];
        fields = new ArrayList<Field>();
        fieldStartX = 0;
        fieldStartY = 0;
        fieldStartCode = (byte) 0xe0;

        for (int y = 0; y < height; y++) {
            final char[] line = decode((String) bufferData.get(y), y, fields);
            if (line.length > width) {
                width = line.length;
            }
            buffer[y] = line;
        }
        // add the final field on the page
        fields.add(createField(fieldStartCode, fieldStartX, fieldStartY, width - 1, height - 1, color, ext_highlight));
    }

    public List<String> getBufferData() {
        return Collections.unmodifiableList(bufferData);
    }

    public void dump(final String filename) {
        try {
            final PrintWriter out = new PrintWriter(new FileWriter(filename));
            for (final Iterator<String> i = bufferData.iterator(); i.hasNext();) {
                out.println(i.next());
            }
            out.println(status);
            out.println("ok");
            out.close();
        } catch (final IOException ex) {
            throw new RuntimeException("error: " + ex);
        }
    }

    private static final Pattern FORMATTED_CHAR_PATTERN = Pattern
            .compile("SF\\((..)=(..)(,(..)=(..)(,(..)=(..))?)?\\)|[0-9a-fA-F]{2}");

    private int fieldStartX = 0;
    private int fieldStartY = 0;
    private byte fieldStartCode = (byte) 0xe0;

    private int color = Field.ATTR_COL_DEFAULT;
    private int ext_highlight = Field.ATTR_EH_DEFAULT;

    /**
     * Decodes a single line from the raw screen buffer dump.
     */
    private char[] decode(String line, final int y, final List<Field> fields) {

        int fieldEndX = 0;
        int fieldEndY = 0;
        int i;
        int auxStartcode = -1;
        int auxColor;
        int auxExthighlight;
        String auxCode;

        if (line.startsWith("data: ")) {
            line = line.substring(6);
        }

        final StringBuffer result = new StringBuffer();
        int index = 0;

        // workaround! delete all extended attributes in a line!
        // must have, until h3270 supports extended attributes
        line = line.replaceAll("SA\\(..=..\\)", "");

        final Matcher m = FORMATTED_CHAR_PATTERN.matcher(line);

        while (m.find()) {
            final String code = m.group();
            if (code.startsWith("SF")) {

                if (!isFormatted) {
                    throw new RuntimeException("format information in unformatted screen");
                }
                result.append(' ');
                i = 1;
                auxColor = -1;
                auxExthighlight = -1;

                while (i <= m.groupCount()) {
                    auxCode = m.group(i);
                    if (auxCode == null) {
                        break;
                    }

                    if (auxCode.equals("c0")) {
                        if (fieldStartX != -1) {
                            // if we've been in an open field, close it now
                            fieldEndX = index - 1;
                            fieldEndY = y;
                            if (fieldEndX == -1) {
                                fieldEndX = width - 1;
                                fieldEndY--;
                            }
                        }
                        auxStartcode = i + 1;
                    } else if (auxCode.equals("41")) {
                        auxExthighlight = i + 1;
                    } else if (auxCode.equals("42")) {
                        auxColor = i + 1;
                    }
                    i = i + 3;
                }

                if (i > 1) {
                    if (fieldStartX != -1) {
                        fields.add(createField(fieldStartCode, fieldStartX, fieldStartY, fieldEndX, fieldEndY, color,
                                ext_highlight));
                    }
                    fieldStartX = index + 1;
                    fieldStartY = y;
                    fieldStartCode = (byte) Integer.parseInt(m.group(auxStartcode), 16);
                    if (auxExthighlight != -1) {
                        ext_highlight = Integer.parseInt(m.group(auxExthighlight), 16);
                    } else {
                        ext_highlight = Field.ATTR_EH_DEFAULT;
                    }
                    if (auxColor != -1) {
                        color = Integer.parseInt(m.group(auxColor), 16);
                    } else {
                        color = Field.ATTR_COL_DEFAULT;
                    }
                }
            } else {
                result.append((char) (Integer.parseInt(code, 16)));
            }
            index++;
        }
        // a field that begins in the last column
        if (fieldStartX == index && fieldStartY == y) {
            fieldStartX = 0;
            fieldStartY++;
        }
        return result.toString().toCharArray();
    }

    private Field createField(final byte startCode, final int startx, final int starty, final int endx, final int endy,
            final int color, final int extHighlight) {
        if ((startCode & Field.ATTR_PROTECTED) == 0 || !isFormatted) {
            return new InputField(this, startCode, startx, starty, endx, endy, color, extHighlight);
        } else {
            return new Field(this, startCode, startx, starty, endx, endy, color, extHighlight);
        }
    }

}
