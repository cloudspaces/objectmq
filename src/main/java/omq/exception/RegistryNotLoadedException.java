/*****************************************************************************************
 * EVO : an Event-Based Object Distributed Middleware
 * 2003-2011 AST Research Group  
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *****************************************************************************************/
package omq.exception;

public class RegistryNotLoadedException extends RemoteException {

	private static final long serialVersionUID = 5721550693129237244L;

public RegistryNotLoadedException (String msg) {
    super (msg);
  }
}
