$(document).ready(function() {
	'use strict';
	var csrf = SolarNode.csrfData;
	var configId = $("meta[name='loxone-config-id']").attr("content");
	var eventUrl = 'ws://' +document.location.host +SolarNode.context.path('/loxone-ws');
	var socket = new WebSocket(eventUrl);
	var client = Stomp.over(socket);
	var headers = {};
	headers[csrf.headerName] = csrf.token;
	client.connect(headers, function(frame) {
		
		var subscription = client.subscribe("/topic/test", function(message) {
			if (message.body) {
				console.log("got message with body %s", message.body)
			} else {
				console.log("got empty message");
			}
		});
		
		client.send("/app/test", {configId:configId}, "Hi.");
	}, function (error) {
        console.log("STOMP protocol error %s", error);
	});
});
