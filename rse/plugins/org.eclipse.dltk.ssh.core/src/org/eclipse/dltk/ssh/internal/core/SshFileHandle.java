package org.eclipse.dltk.ssh.internal.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.ssh.core.ISshFileHandle;

import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SshFileHandle implements ISshFileHandle {
	private SshConnection connection = null;
	private IPath path;
	private IPath linkTarget;
	private SftpATTRS attrs;
	private Map<String, SshFileHandle> children = new HashMap<String, SshFileHandle>();
	private boolean childrenFetched = false;

	public SshFileHandle(SshConnection connection, IPath path, SftpATTRS attrs) {
		this.connection = connection;
		this.path = path;
		this.attrs = attrs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.dltk.ssh.internal.core.ISshFileHandle#createFolder(java.lang
	 * .String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISshFileHandle createFolder(String newEntryName,
			IProgressMonitor monitor) throws CoreException {
		ISshFileHandle child = (ISshFileHandle) getChild(newEntryName);
		if (child != null) {
			child.mkdir();
			fetchAttrs();
		}
		return child;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.dltk.ssh.internal.core.ISshFileHandle#mkdir()
	 */
	public void mkdir() {
		connection.mkdir(path);
		fetchAttrs(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.dltk.ssh.internal.core.ISshFileHandle#delete()
	 */
	public void delete() throws CoreException {
		fetchAttrs();
		if (attrs != null) {
			connection.delete(path, attrs.isDir());
			fetchAttrs(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.dltk.ssh.internal.core.ISshFileHandle#exists()
	 */
	public boolean exists() {
		fetchAttrs();
		return attrs != null;
	}

	private void fetchAttrs() {
		fetchAttrs(false);
	}

	private void fetchAttrs(boolean clean) {
		if (attrs == null || clean) {
			attrs = connection.getAttrs(path);
		}
		if (attrs != null && attrs.isLink()) {
			attrs = connection.getAttrs(path);
			this.linkTarget = connection.getResolvedPath(path);
		}
	}

	public synchronized ISshFileHandle getChild(String newEntryName) {
		if (children.containsKey(newEntryName)) {
			return children.get(newEntryName);
		}
		ISshFileHandle child = new SshFileHandle(connection, path
				.append(newEntryName), null);
		return child;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.dltk.ssh.internal.core.ISshFileHandle#getChildren(org.eclipse
	 * .core.runtime.IProgressMonitor)
	 */
	public synchronized ISshFileHandle[] getChildren(IProgressMonitor monitor)
			throws CoreException {
		if (!childrenFetched) {
			// Fetch all child handles
			fetchChildren();
		}
		return children.values().toArray(new SshFileHandle[children.size()]);

	}

	private void fetchChildren() {
		Vector list = connection.list(path);
		if (list != null) {
			children.clear();
			for (Object object : list) {
				LsEntry entry = (LsEntry) object;
				String filename = entry.getFilename();
				if (filename.equals(".") || filename.equals("..")) {
					continue;
				}
				SftpATTRS childAttrs = entry.getAttrs();
				SshFileHandle childHandle = new SshFileHandle(connection, path
						.append(filename), childAttrs);
				children.put(filename, childHandle);
			}
			childrenFetched = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.dltk.ssh.internal.core.ISshFileHandle#getInputStream(org.
	 * eclipse.core.runtime.IProgressMonitor)
	 */
	public InputStream getInputStream(IProgressMonitor monitor)
			throws CoreException {
		fetchAttrs();
		if (attrs != null) {
			IPath current = this.path;
			if (attrs.isLink() && linkTarget != null) {
				current = linkTarget;
			}
			final InputStream stream = connection.get(this.path);
			if (stream != null) {
				InputStream wrapperStream = new BufferedInputStream(stream) {
					@Override
					public void close() throws IOException {
						super.close();
						// fetchAttrs(true);
					}
				};
				return wrapperStream;
			}
			return stream;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.dltk.ssh.internal.core.ISshFileHandle#getName()
	 */
	public String getName() {
		return path.lastSegment();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.dltk.ssh.internal.core.ISshFileHandle#getOutputStream(org
	 * .eclipse.core.runtime.IProgressMonitor)
	 */
	public OutputStream getOutputStream(IProgressMonitor monitor)
			throws CoreException {
		final OutputStream stream = connection.put(this.path);
		if (stream != null) {
			OutputStream wrapperStream = new BufferedOutputStream(stream) {
				@Override
				public void close() throws IOException {
					super.close();
					fetchAttrs(true);
				}
			};
			return wrapperStream;
		}
		return stream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.dltk.ssh.internal.core.ISshFileHandle#isDirectory()
	 */
	public boolean isDirectory() {
		fetchAttrs();
		if (attrs != null) {
			return attrs.isDir();
		} else {
			// IStatus status = new Status(IStatus.ERROR, "blah",
			// "Failed to retrive file information:" + path);
			// throw new CoreException(status);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.dltk.ssh.internal.core.ISshFileHandle#lastModificationTime()
	 */
	public long lastModificationTime() {
		fetchAttrs();
		if (attrs != null) {
			return attrs.getMTime() * 1000L;
		} else {
			// IStatus status = new Status(IStatus.ERROR, "blah",
			// "Failed to retrive file information:" + path);
			// throw new CoreException(status);
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.dltk.ssh.internal.core.ISshFileHandle#setLastModified(long,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void setLastModified(long timestamp, IProgressMonitor monitor)
			throws CoreException {
		connection.setLastModified(path, timestamp);
		fetchAttrs(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.dltk.ssh.internal.core.ISshFileHandle#getSize()
	 */
	public long getSize() {
		fetchAttrs();
		if (attrs != null) {
			return attrs.getSize();
		}
		return 0;
	}

	public boolean isSymlink() {
		fetchAttrs();
		if (attrs != null) {
			return attrs.isLink();
		}
		return false;
	}
}