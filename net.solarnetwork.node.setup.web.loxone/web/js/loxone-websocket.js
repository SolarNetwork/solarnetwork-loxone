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
	
	(function() {
		var csrf = SolarNode.csrfData;
		var url = 'ws://' +document.location.host +SolarNode.context.path('/loxone-ws');
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
			
			// TODO: any need to hang on to subscription result?
			var subscription = client.subscribe('/app/'+configId+'/events/values', function(message) {
				defaultHandleDataMessage(message, processValueEvents);
			});
			
		}, function (error) {
	        console.log("STOMP protocol error %s", error);
		});
	})();
});
