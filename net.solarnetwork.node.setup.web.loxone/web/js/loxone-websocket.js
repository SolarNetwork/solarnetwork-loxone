$(document).ready(function() {
	'use strict';
	var csrf = SolarNode.csrfData;
	var configId = $("meta[name='loxone-config-id']").attr("content");
	var eventUrl = 'ws://' +document.location.host +SolarNode.context.path('/a/loxone-ws');
	var socket = new WebSocket(eventUrl);
	var stompClient = Stomp.over(socket);
	var headers = {};
	headers[csrf.headerName] = csrf.token;
	stompClient.connect(headers, function(frame) {
		stompClient.send("/app/test", {configId:configId}, "Hi.");
		//var subscription = stompClient.subscribe("/queue/test", callback);
	});
});
