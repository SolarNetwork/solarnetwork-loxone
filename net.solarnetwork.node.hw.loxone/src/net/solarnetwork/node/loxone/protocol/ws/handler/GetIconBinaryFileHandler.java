/* ==================================================================
 * GetIconBinaryFileHandler.java - 23/09/2016 4:56:26 PM
 * 
 * Copyright 2007-2016 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.node.loxone.protocol.ws.handler;

import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.websocket.Session;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import net.solarnetwork.node.RemoteServiceException;
import net.solarnetwork.node.loxone.protocol.ws.BinaryFileHandler;
import net.solarnetwork.node.loxone.protocol.ws.CommandType;
import net.solarnetwork.node.loxone.protocol.ws.MessageHeader;

/**
 * Request and handle image resources.
 * 
 * @author matt
 * @version 1.0
 */
public class GetIconBinaryFileHandler extends BaseCommandHandler implements BinaryFileHandler {

	private static final byte[] PNG_HEADER = new byte[] { (byte) 0x89, (byte) 0x50, (byte) 0x4E,
			(byte) 0x47 };

	// create a fixed size queue for handling GetIcon requests
	private final ConcurrentMap<Long, BlockingQueue<String>> iconQueue = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, GetImageFuture> iconRequests = new ConcurrentHashMap<>(10);

	@Override
	public boolean supportsCommand(CommandType command) {
		return (command == CommandType.GetIcon);
	}

	@Override
	public Future<?> sendCommand(CommandType command, Session session, Object... args)
			throws IOException {
		// we need one and only one argument: the name of the image to load
		Long configId = getConfigId(session);
		if ( supportsCommand(command) && args != null && args.length > 0 && args[0] != null ) {
			log.trace("Requesting image [{}]", args[0]);
			return requestImage(session, configId, args[0].toString());
		}
		return null;
	}

	private Future<Resource> requestImage(Session session, Long configId, String name) {
		BlockingQueue<String> queue = iconQueue.get(configId);
		if ( queue == null ) {
			queue = new ArrayBlockingQueue<>(5);
			BlockingQueue<String> existingQueue = iconQueue.putIfAbsent(configId, queue);
			if ( existingQueue != null ) {
				queue = existingQueue;
			}
		}
		try {
			if ( !queue.offer(name, 1, TimeUnit.MINUTES) ) {
				throw new RemoteServiceException("Timeout waiting to request image [" + name + "]");
			}
		} catch ( InterruptedException e ) {
			throw new RemoteServiceException("Interrupted waiting to request image [" + name + "]");
		}
		try {
			GetImageFuture req = new GetImageFuture(name);
			GetImageFuture oldReq = iconRequests.put(name, req);
			if ( oldReq != null ) {
				oldReq.cancel(true);
			}
			session.getBasicRemote().sendText(name);
			return req;
		} catch ( IOException e ) {
			queue.poll();
			throw new RemoteServiceException("Error requesting image [" + name + "]", e);
		}
	}

	private final class GetImageFuture implements Future<Resource> {

		private final CountDownLatch latch;
		private final String name;
		private boolean cancelled;
		private Resource resource;

		private GetImageFuture(String name) {
			super();
			this.latch = new CountDownLatch(1);
			this.name = name;
		}

		/**
		 * Set the image resource.
		 * 
		 * @param resource
		 *        The resolved image resource.
		 */
		private void setResource(Resource resource) {
			if ( !isDone() ) {
				this.resource = resource;
				latch.countDown();
			}
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if ( !cancelled ) {
				iconRequests.remove(name, this);
				cancelled = true;
			}
			return cancelled;
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public boolean isDone() {
			return (isCancelled() || latch.getCount() < 1);
		}

		@Override
		public Resource get() throws InterruptedException, ExecutionException {
			latch.await();
			return resource;
		}

		@Override
		public Resource get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			latch.await(timeout, unit);
			return resource;
		}

	}

	@Override
	public boolean supportsDataMessage(MessageHeader header, ByteBuffer buffer) {
		if ( (buffer.limit() - buffer.position()) < PNG_HEADER.length ) {
			return false;
		}
		byte[] magic = new byte[PNG_HEADER.length];
		buffer.get(magic);
		return Arrays.equals(PNG_HEADER, magic);
	}

	@Override
	public boolean handleDataMessage(MessageHeader header, Session session, ByteBuffer buffer) {
		Long configId = getConfigId(session);
		byte[] data = new byte[buffer.remaining()];
		buffer.get(data);
		handleImageData(configId, data);
		return true;
	}

	private void handleImageData(Long configId, byte[] data) {
		String name = nextImageName(configId);
		log.debug("Got image {}", name);
		GetImageFuture f = iconRequests.remove(name);
		if ( f != null ) {
			f.setResource(new ByteArrayIconResource(data, name));
		}
	}

	/**
	 * An in-memory {@link Resource} that supports a filename with in-memory
	 * data.
	 */
	private static final class ByteArrayIconResource extends ByteArrayResource {

		private final String filename;

		private ByteArrayIconResource(byte[] data, String filename) {
			super(data);
			this.filename = filename;
		}

		@Override
		public String getFilename() {
			return filename;
		}
	}

	private String nextImageName(Long configId) {
		BlockingQueue<String> queue = iconQueue.get(configId);
		return (queue != null ? queue.poll() : null);
	}

	@Override
	public boolean supportsTextMessage(MessageHeader header, Reader reader, int limit)
			throws IOException {
		// read at most 256 to inspect what we have, we'll look for "<svg"
		char[] buf = new char[limit > 256 ? 256 : limit];
		int count = reader.read(buf, 0, buf.length);
		String s = new String(buf, 0, count);
		return s.contains("<svg");
	}

	@Override
	public boolean handleTextMessage(MessageHeader header, Session session, Reader reader)
			throws IOException {
		Long configId = getConfigId(session);
		String s = FileCopyUtils.copyToString(reader);
		log.debug("Got SVG image {}", s);
		handleImageData(configId, s.getBytes());
		return true;
	}

}
