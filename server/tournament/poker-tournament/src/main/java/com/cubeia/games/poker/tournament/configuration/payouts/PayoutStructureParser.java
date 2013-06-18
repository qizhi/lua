/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.games.poker.tournament.configuration.payouts;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Double.parseDouble;
import static java.math.BigDecimal.valueOf;

/**
 * Parser for parsing payout structures.
 *
 * The format is:
 *
 * First line: position ranges, for example 1, 2, 3, 4-5, 6-7 (means that position 4-5 get the same prize and 6-7 get the same prize).
 * Following lines: number of players in tournament followed by payouts in percent.
 * Example: 2-2 100
 *          3-5 67, 33
 * Means, for two players, winner gets all, for 3-7 players, 1st place gets 67% and second place gets 33%.
 *
 */
public class PayoutStructureParser {

    private static final Logger log = Logger.getLogger(PayoutStructureParser.class);

    public PayoutStructure parsePayouts(String csvFile) {
        try {
            return parseFile(csvFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing csvFile " + csvFile, e);
        }
    }

    public PayoutStructure parsePayouts(File file) {
        try {
            return parse(new BufferedReader(new FileReader(file)));
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing file " + file.getAbsolutePath(), e);
        }
    }

    public PayoutStructure parsePayouts(InputStream stream) {
        try {
            return parse(new BufferedReader(new InputStreamReader(stream)));
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing stream.", e);
        }
    }

    private PayoutStructure parseFile(String csvFile) throws IOException {
        log.debug("Parsing file " + csvFile);
        BufferedReader reader = openFile(csvFile);
        return parse(reader);
    }

    private BufferedReader openFile(String csvFile) {
        try {
            InputStream stream = getClass().getResourceAsStream(csvFile);
            return new BufferedReader(new InputStreamReader(stream));
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot open file " + csvFile, e);
        }
    }

    private PayoutStructure parse(BufferedReader reader) throws IOException {
        String positions = reader.readLine();
        List<IntRange> payoutRanges = parsePayoutRanges(positions);
        List<Payouts> payoutTable = parsePayouts(reader, payoutRanges);
        return new PayoutStructure(payoutTable);
    }

    private List<Payouts> parsePayouts(BufferedReader reader, List<IntRange> payoutRanges) throws IOException {
        /*
        * Parse the lines defining the payouts for each range of entrants. If the line starts with 2-3, it means that line describes the payouts
        * for tournaments with 2 or 3 players.
        */
        String line = reader.readLine();
        List<Payouts> payoutTable = newArrayList();
        while (line != null) {
            List<Payout> payouts = newArrayList();
            StringTokenizer payoutsTokenizer = new StringTokenizer(line, ",");
            if (payoutsTokenizer.hasMoreTokens()) {
                String playerRange = payoutsTokenizer.nextToken();
                IntRange entrants = parseRange(playerRange);

                // The following tokens contain the payout for each range of positions.
                int payoutRangeIndex = 0;
                while (payoutsTokenizer.hasMoreTokens()) {
                    IntRange payoutRange = payoutRanges.get(payoutRangeIndex++);
                    String percentageString = sanitize(payoutsTokenizer.nextToken());
                    BigDecimal percentage = valueOf(parseDouble(percentageString));
                    Payout payout = new Payout(payoutRange, percentage);
                    payouts.add(payout);
                }
                payoutTable.add(new Payouts(entrants, payouts));
            }
            line = reader.readLine();
        }
        return payoutTable;
    }

    private List<IntRange> parsePayoutRanges(String positions) {
        // Parse the first line, which contains the ranges we payout to. 14-28 means all players from place 14 to 28 get one payout.
        StringTokenizer tokenizer = new StringTokenizer(positions, ",");
        List<IntRange> payoutRanges = new ArrayList<IntRange>();
        while (tokenizer.hasMoreTokens()) {
            String entrants = sanitize(tokenizer.nextToken());
            if (!validRange(entrants)) {
                log.debug("Invalid range " + entrants + ", ignoring.");
                continue;
            }
            IntRange range;
            if (entrants.contains("-")) {
                range = parseRange(entrants);
            } else {
                int start = Integer.parseInt(entrants);
                range = new IntRange(start, start);
            }
            payoutRanges.add(range);
        }
        return payoutRanges;
    }

    private String sanitize(String string) {
        return string.replaceAll("\"", "").replaceAll("%", "").trim();
    }

    // Checks if the range is either just a number or a number followed by a dash and a number. (I.e. 15 is valid and 15-17 is valid)
    private boolean validRange(String entrants) {
        return Pattern.compile("[0-9]+(-[0-9]*)?").matcher(entrants).matches();
    }

    private IntRange parseRange(String entrants) {
        IntRange range;
        String stripped = entrants.replaceAll("\"", "").replaceAll("%", "").trim();
        String[] startStop = stripped.split("-");
        int start = Integer.parseInt(startStop[0]);
        int stop = Integer.parseInt(startStop[1]);
        range = new IntRange(start, stop);
        return range;
    }
}
