$(document).ready(function() {
	'use strict';
	
	$('button.loxone-console-open').on('click', function(event) {
		var btn = $(event.target),
			configId = btn.parent().data('config-id'),
			destUrl = SolarNode.context.path('/a/loxone/'+configId);
		alert('window.location to ' + destUrl);
	});

});
