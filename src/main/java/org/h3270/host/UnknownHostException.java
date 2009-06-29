/*
 * Copyright (C) 2003-2007 akquinet framework solutions
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

package org.h3270.host;

/**
 * Indicates that the name of the mainframe host could not be resolved. This is most likely due to a typing error in the
 * connect dialog.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id: UnknownHostException.java,v 1.2 2007/03/02 09:35:44 spiegel Exp $
 */
public class UnknownHostException extends S3270Exception {

    private static final long serialVersionUID = -4523691701264020588L;

    private String host;

    public UnknownHostException(final String host) {
        super(host + " is unknown");
        this.host = host;
    }

    /**
     * Returns the name of the host that could not be resolved.
     */
    public String getHost() {
        return host;
    }

}
