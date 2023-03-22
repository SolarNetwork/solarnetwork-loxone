$(document).ready(function() {
	'use strict';
	
	function setupLoxoneUiLauncher(container) {
		container.find('button.loxone-console-open').on('click', function(event) {
			var btn = $(event.target),
				configID = btn.parent().data('config-id');
			if ( configID ) {	
				window.location.replace(SolarNode.context.path(`/a/loxone/${configID}`));
			} else {
				alert('Loxone configuration not available; make sure Loxone is available and then refresh this page to try again.');
			}
		});
	}
	
	$('body').on('sn.settings.component.loaded', function(event, container) {
		setupLoxoneUiLauncher(container);
	});	

	setupLoxoneUiLauncher($());
});
