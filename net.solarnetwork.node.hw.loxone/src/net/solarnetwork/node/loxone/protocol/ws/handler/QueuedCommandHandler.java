/* ==================================================================
 * QueuedCommandHandler.java - 12/06/2017 9:30:08 AM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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
import net.solarnetwork.node.RemoteServiceException;
import net.solarnetwork.node.loxone.protocol.ws.CommandHandler;

/**
 * Support {@link CommandHandler} for future-based request/response exchanges.
 * 
 * <p>
 * Queues of requests are managed per {@literal configId}, so that multiple
 * Loxone devices can be handled.
 * </p>
 * 
 * @author matt
 * @version 1.0
 * @since 1.1
 */
public abstract class QueuedCommandHandler<K, V> extends BaseCommandHandler {

	private static final int DEFAULT_QUEUE_SIZE = 5;

	private final int queueSize;
	private final ConcurrentMap<Long, BlockingQueue<K>> queues;
	private final ConcurrentMap<K, LatchBasedFuture> requests;

	/**
	 * Construct with a default queue size.
	 */
	public QueuedCommandHandler() {
		this(DEFAULT_QUEUE_SIZE);
	}

	/**
	 * Construct with a queue size.
	 * 
	 * @param queueSize
	 *        the queue size
	 */
	public QueuedCommandHandler(int queueSize) {
		queues = new ConcurrentHashMap<>();
		requests = new ConcurrentHashMap<>(10);
		this.queueSize = queueSize;
	}

	/**
	 * A simple {@link Future} using a {@link CoundDownLatch} for
	 * synchronization of the results.
	 */
	private final class LatchBasedFuture implements Future<V> {

		private final CountDownLatch latch;
		private final K name;
		private boolean cancelled;
		private V result;

		private LatchBasedFuture(K name) {
			super();
			this.latch = new CountDownLatch(1);
			this.name = name;
		}

		/**
		 * Set the result object.
		 * 
		 * @param result
		 *        The resolved object.
		 */
		private void setResult(V result) {
			if ( !isDone() ) {
				this.result = result;
				latch.countDown();
			}
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if ( !cancelled ) {
				requests.remove(name, this);
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
		public V get() throws InterruptedException, ExecutionException {
			latch.await();
			return result;
		}

		@Override
		public V get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			latch.await(timeout, unit);
			return result;
		}

	}

	/**
	 * Set a future-based text request.
	 * 
	 * <p>
	 * Calling this method will queue a {@link Future} for the provided
	 * {@code key}. Later on, when the response comes (for example, via
	 * {@link CommandHandler#handleCommand(net.solarnetwork.node.loxone.protocol.ws.CommandType, net.solarnetwork.node.loxone.protocol.ws.MessageHeader, Session, com.fasterxml.jackson.databind.JsonNode)})
	 * then {@link #handleNextResult(Long, V)} can be called to add the result
	 * to the {@code Future}.
	 * </p>
	 * 
	 * <p>
	 * If a request exists already for {@code key} then that request will be
	 * cancelled via {@link Future#cancel(boolean)}.
	 * </p>
	 * 
	 * @param session
	 *        the websocket session
	 * @param configId
	 *        the Loxone config ID
	 * @param key
	 *        the unique key for this message (could be the same as
	 *        {@code text})
	 * @param text
	 *        the text to send
	 * @param request
	 *        the request future to handle the response
	 */
	protected Future<V> sendTextForKey(Session session, Long configId, K key, String text) {
		BlockingQueue<K> queue = queues.get(configId);
		if ( queue == null ) {
			queue = new ArrayBlockingQueue<>(queueSize);
			BlockingQueue<K> existingQueue = queues.putIfAbsent(configId, queue);
			if ( existingQueue != null ) {
				queue = existingQueue;
			}
		}
		try {
			if ( !queue.offer(key, 1, TimeUnit.MINUTES) ) {
				throw new RemoteServiceException("Timeout waiting to request [" + text + "]");
			}
		} catch ( InterruptedException e ) {
			throw new RemoteServiceException("Interrupted waiting to request [" + text + "]");
		}
		try {
			LatchBasedFuture request = new LatchBasedFuture(key);
			LatchBasedFuture oldReq = requests.put(key, request);
			if ( oldReq != null ) {
				oldReq.cancel(true);
			}
			session.getBasicRemote().sendText(text);
			return request;
		} catch ( IOException e ) {
			queue.poll();
			throw new RemoteServiceException("Error requesting [" + text + "]", e);
		}
	}

	/**
	 * See what the next result key in the queue is, leaving it as the next
	 * result key.
	 * 
	 * @param configId
	 *        the config ID of the queue to manage
	 * @return the next result key, or {@literal null} if not available
	 */
	protected K peekNextResultKey(Long configId) {
		BlockingQueue<K> queue = queues.get(configId);
		return (queue != null ? queue.peek() : null);
	}

	/**
	 * Get the next result key is, removing it from the queue.
	 * 
	 * @param configId
	 *        the config ID of the queue to manage
	 * @return the next result key, or {@literal null} if not available
	 */
	protected K popNextResultKey(Long configId) {
		BlockingQueue<K> queue = queues.get(configId);
		return (queue != null ? queue.poll() : null);
	}

	/**
	 * Handle the next result for the queue.
	 * 
	 * <p>
	 * This will call {@link #popNextResultKey(Long)} to get the next result
	 * key, and then set {@code result} on the {@link Future} associated with
	 * that key.
	 * </p>
	 * 
	 * @param configId
	 *        the config ID of the queue to manage
	 * @param result
	 *        the result value to set
	 * @return the {@code Future} associated with the queue, with the result set
	 */
	protected Future<V> handleNextResult(Long configId, V result) {
		K key = popNextResultKey(configId);
		log.debug("Got result {}: {}", key, result);
		LatchBasedFuture f = requests.remove(key);
		if ( f != null ) {
			f.setResult(result);
		}
		return f;
	}

}