/*
 * WordNet-Java
 *
 * Copyright 1998 by Oliver Steele.  You can use this software freely so long as you preserve
 * the copyright notice and this restriction, and label your changes.
 */
package edu.brandeis.cs.steele.wn;
import java.io.IOException;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;

/** An object of this class can serve as a file manager for remote <CODE>FileBackedDictionary</CODE>
 * instantiations, using RMI.  This class also contains utility routines to publish a <code>RemoteFileManager</code> for remote use,
 * and to lookup a remote one for local use.
 * 
 * <P>To make a <CODE>RemoteFileManager</CODE> available to remote clients:
 * <PRE>
 *   System.setSecurityManager(new RMISecurityManager());
 *   LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
 *   new RemoteFileManager().bind();
 * </PRE>
 *
 * <P>To create a local <CODE>DictionaryDatabase</CODE> backed by a remote <CODE>RemoteFileManager</CODE>:
 * <PRE>
 *   DictionaryDatabase dictionary = new FileBackedDictionary(RemoteFileManager.lookup(hostname));
 * </PRE>
 * 
 * @author Oliver Steele, steele@cs.brandeis.edu
 * <BR>Copyright 1998 Oliver Steele, see <A HREF="http://www.cs.brandeis.edu/~steele/WNJ/license.html">http://www.cs.brandeis.edu/~steele/WNJ/license.html</A>.
 * @version 1.0
 */
public class RemoteFileManager extends FileManager implements Remote {
	/** The standard RMI binding name. */
	public static final String BINDING_NAME = "edu.brandeis.cs.steele.wn" + FileManager.VERSION;
	
	/** Construct a file manager backed by a set of files contained in the default WN search directory.
	 * See {@link FileManager} for a description of the default search directory.
	 * @exception RemoteException If remote operation failed.
	 */
	public RemoteFileManager() throws RemoteException {
		super();
		UnicastRemoteObject.exportObject(this);
	}
	
	/** Construct a file manager backed by a set of files contained in <var>searchDirectory</var>.
	 * @exception RemoteException If remote operation failed.
	 */
	public RemoteFileManager(String searchDirectory) throws RemoteException {
		super(searchDirectory);
		UnicastRemoteObject.exportObject(this);
	}
	
	/** Bind this object to the value of <code>BINDING_NAME</code> in the local RMI
	 * registry.
	 * @exception AlreadyBoundException If <code>BINDING_NAME</code> is already bound.
	 * @exception RemoteException If remote operation failed.
	 */
	public void bind() throws RemoteException, AlreadyBoundException {
		Registry registry = LocateRegistry.getRegistry();
		registry.bind(BINDING_NAME, this);
	}
	
	/** Lookup the object bound to the value of <code>BINDING_NAME</code> in the RMI
	 * registry on the host named by <var>hostname</var>
	 * @return An RMI proxy of type <code>FileManagerInterface</code>.
	 * @exception AccessException If this operation is not permitted.
	 * @exception NotBoundException If there is no object named <code>BINDING_NAME</code> in the remote registry.
	 * @exception RemoteException If remote operation failed.
	 * @exception UnknownHostException  If the host could not be located.
	 */
	public static FileManagerInterface lookup(String hostname) throws AccessException, NotBoundException, RemoteException, UnknownHostException {
		Registry registry = LocateRegistry.getRegistry(hostname);
		return (FileManagerInterface) registry.lookup(BINDING_NAME);
	}
}
