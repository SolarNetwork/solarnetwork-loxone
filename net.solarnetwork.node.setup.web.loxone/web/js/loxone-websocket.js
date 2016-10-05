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

			// subscribe to /app/X/events/values to get a complete list of all available values
			var allValueEventsSubscription = client.subscribe(`/app/${Loxone.configID}/events/values`, function(message) {
				defaultHandleDataMessage(message, processValueEvents);
				// once we've downloaded all events, we can unsubscribe from this channel as we'll
				// pick up updates via the /topic/X/events/values subscription below
				allValueEventsSubscription.unsubscribe();
			});

			// subscribe to /topic/X/events/values to get notified of updated values
			var valueEventUpdates = client.subscribe(`/topic/${Loxone.configID}/events/values`, function(message) {
				defaultHandleDataMessage(message, processValueEvents);
			});

			// subscribe to /topic/X/events/text to get notified of text event updates
			var textEventUpdates = client.subscribe(`/topic/${Loxone.configID}/events/texts`, function(message) {
				
			});

			// add a periodic call to /a/loxone/ping so the HTTP session stays alive;
			// TODO: this may be undersirable, as a logged in user will forever stay logged in
			setInterval(function() {
				Loxone.api.ping(function(err, response) {
					if(err) return console.log(`Error pinging: ${err}`);
					if(!response.success) console.log('Ping failed');
				})
			},60000);

		}, function (error) {
	    console.log('STOMP protocol error %s', error);
		});
	})();
})();
