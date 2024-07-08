Loxone.websocket = (function() {

	function processValueEvents(list) {
		list.forEach(function(ve) {

      var valueID = `value-${ve.uuid}`
      var value = ve.value.toFixed(2);

      if(this.controlView.viewModel[valueID] == null) {
        this.controlView.viewModel[valueID] = ko.observable(value);
      } else {
        this.controlView.viewModel[valueID](value);
      }

			var timeID = `time-${ve.uuid}`;

			var formattedTime = this.formatDurationDate(ve.created);

      if(this.controlView.viewModel[timeID] == null) {
        this.controlView.viewModel[timeID] = ko.observable(formattedTime);
      } else {
        this.controlView.viewModel[timeID](formattedTime);
      }

		});
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

			// Call /a/X/events/values to get a complete list of all available values
		    Loxone.api.request({ method: 'GET', path: 'events/values', headers: {'Content-Type': 'application/json' }}, function(err, json) {
					if(err || !json.success) console.log(`Error getting initial events/values: ${err}`);
			    else processValueEvents(json.data);

					// subscribe to /topic/X/events/values to get notified of updated values
					var valueEventUpdates = client.subscribe(`/topic/${Loxone.configID}/events/values`, function(message) {
						defaultHandleDataMessage(message, processValueEvents);
					});
		    });


			// add a periodic call to /a/loxone/ping so the HTTP session stays alive;
			// TODO: this may be undersirable, as a logged in user will forever stay logged in
			setInterval(function() {
				Loxone.api.ping(function(err, response) {
					if(err) return console.log(`Error pinging: ${err}`);
					if(!response || !response.success) console.log('Ping failed');
				})
			},60000);

		}, function (error) {
	    	console.log('STOMP protocol error %s', error);
		});
	})();
})();
