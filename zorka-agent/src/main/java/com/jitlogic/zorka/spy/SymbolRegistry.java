/**
 * Copyright 2012-2013 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
 * <p/>
 * This is free software. You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p/>
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jitlogic.zorka.spy;

import com.jitlogic.zorka.util.ZorkaLog;
import com.jitlogic.zorka.util.ZorkaLogConfig;
import com.jitlogic.zorka.util.ZorkaLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks information on all symbol strings used by tracer. These are mainly trace names,
 * class names, method names and method signature strings. Maintains name-to-ID maps, so
 * tracer can use just integer IDs internally but present human-readable names when necessary.
 *
 * @author rafal.lewczuk@jitlogic.com
 */
public class SymbolRegistry {

    /** Logger */
    private static final ZorkaLog log = ZorkaLogger.getLog(SymbolRegistry.class);

    /** ID of last symbol added to registry. */
    private AtomicInteger lastId = new AtomicInteger(0);

    /** Symbol name to ID map */
    private ConcurrentHashMap<String,Integer> symbols = new ConcurrentHashMap<String, Integer>();

    /** Symbol ID to name map */
    private ConcurrentHashMap<Integer,String> idents = new ConcurrentHashMap<Integer,String>();


    /**
     * Returns ID of named symbol. If symbol hasn't been registered yet,
     * it will be and new ID will be assigned for it.
     *
     * @param symbol symbol name
     *
     * @return symbol ID (integer)
     */
    public int symbolId(String symbol) {
        Integer id = symbols.get(symbol);

        if (id == null) {
            int newid = lastId.incrementAndGet();

            if (ZorkaLogConfig.isTracerLevel(ZorkaLogConfig.ZTR_SYMBOL_REGISTRY)) {
                log.debug("Adding symbol '" + symbol + "', newid=" + newid);
            }

            id = symbols.putIfAbsent(symbol, newid);
            if (id == null) {
                idents.put(newid, symbol);
                id = newid;
            }
        }

        return id;
    }


    /**
     * Returns symbol name based on ID or null if no such symbol has been registered.
     *
     * @param symbolId symbol ID
     *
     * @return symbol name
     */
    public String symbolName(int symbolId) {
        return idents.get(symbolId);
    }


    /**
     * Adds new symbol to registry (with predefined ID).
     *
     * @param symbolId symbol ID
     *
     * @param symbol symbol name
     */
    public void put(int symbolId, String symbol) {

        if (ZorkaLogConfig.isTracerLevel(ZorkaLogConfig.ZTR_SYMBOL_REGISTRY)) {
            log.debug("Putting symbol '" + symbol + "', newid=" + symbolId);
        }

        symbols.put(symbol, symbolId);
        idents.put(symbolId, symbol);

        // TODO not thread safe !
        if (symbolId > lastId.get()) {
            lastId.set(symbolId);
        }
    }


    /**
     * Returns ID of last registered symbol.
     *
     * @return last symbol ID
     */
    public int lastId() {
        return lastId.get();
    }
}
