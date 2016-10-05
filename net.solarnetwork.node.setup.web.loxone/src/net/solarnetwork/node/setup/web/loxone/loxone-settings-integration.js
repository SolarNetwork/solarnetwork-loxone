$(document).ready(function() {
	'use strict';
	
	$('button.loxone-console-open').on('click', function(event) {
		var btn = $(event.target),
			configID = btn.parent().data('config-id');
		window.location.replace(SolarNode.context.path(`/a/loxone/${configID}`));
	});

});
