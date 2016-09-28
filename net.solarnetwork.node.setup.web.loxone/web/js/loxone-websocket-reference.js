$(document).ready(function() {
	'use strict';
	var configId = $("meta[name='loxone-config-id']").attr("content");
	
	function processValueEvents(list) {
		var container = $('#loxone-event-console'),
			group = $('<section><h2>' + new Date()+'</h2></section>');
		console.log('Got value events: %s', JSON.stringify(list, null, 2));
		list.forEach(function(ve) {
			$('<div class="alert alert-info"/>').html('<b>'+ve.uuid +'</b> = ' +ve.value).appendTo(group);
		});
		container.prepend(group);
	}
	
	function processTextEvents(list) {
		var container = $('#loxone-event-console'),
		group = $('<section><h2>' + new Date()+'</h2></section>');
		console.log('Got text events: %s', JSON.stringify(list, null, 2));
		list.forEach(function(te) {
			$('<div class="alert"/>').html('<b>'+te.uuid +'</b> = ' +te.text).appendTo(group);
		});
		container.prepend(group);
	}
	
	(function() {
		var csrf = SolarNode.csrfData;
		var url = 'ws://' +document.location.host +SolarNode.context.path('/ws');
		var socket = new WebSocket(url);
		var client = Stomp.over(socket);
		client.debug = null;
		var headers = {};
		headers[csrf.headerName] = csrf.token;
		client.connect(headers, function(frame) {
			
			function defaultHandleDataMessage(message, successHandler, errorHandler) {
				if ( message.body ) {
					var json = JSON.parse(message.body);
					if ( json.success ) {
						if ( typeof successHandler === 'function' ) {
							successHandler(json.data);
						}
					} else if ( typeof errorHandler === 'function' ) {
						errorHandler(json);
					}
				} else {
					console.log("got empty message");
				}
			}
			
			// subscribe to /app/X/events/values to get a complete list of all available values
			var allValueEventsSubscription = client.subscribe('/app/' +configId+'/events/values', function(message) {
				defaultHandleDataMessage(message, processValueEvents);
				// once we've downloaded all events, we can unsubscribe from this channel as we'll 
				// pick up updates via the /topic/X/events/values subscription below
				allValueEventsSubscription.unsubscribe();
			});
			
			// subscribe to /topic/X/events/values to get notified of updated values
			var valueEventsUpdates = client.subscribe('/topic/' +configId+'/events/values', function(message) {
				defaultHandleDataMessage(message, processValueEvents);
			});
			
			// subscribe to /topic/X/events/text to get notified of text event updates
			var valueEventsUpdates = client.subscribe('/topic/' +configId+'/events/texts', function(message) {
				defaultHandleDataMessage(message, processTextEvents);
			});
			
			// add a periodic call to /a/loxone/ping so the HTTP session stays alive;
			// TODO: this may be undersirable, as a logged in user will forever stay logged in
			setInterval(function() {
				$.getJSON(SolarNode.context.path('/a/loxone/ping'), function(json) {
					console.log('Ping result: %s', json.success);
				});
			},60000);
			
		}, function (error) {
	        console.log("STOMP protocol error %s", error);
		});
	})();
});
