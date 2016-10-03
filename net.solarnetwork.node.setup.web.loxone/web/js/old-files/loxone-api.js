function API(configID) {

	this.configID = document.querySelector('meta[name=\'loxone-config-id\']').getAttribute('content');

	this.url = SolarNode.context.path('/a/loxone/');

	this.request = function(method, url, headers, body, csrf, next) {
		var xmlhttp = new XMLHttpRequest();
		xmlhttp.onreadystatechange = function() {
			if(this.readyState == 4) {
				if(this.status == 200) {
					next(null, JSON.parse(this.responseText));
				} else {
					next(this.responseText);
				}
			}
		};
		xmlhttp.open(method, url, true);
		if(headers != null) {
			for(h in headers) {
				xmlhttp.setRequestHeader(h, headers[h]);
			}
		}
		if(csrf) {
			SolarNode.csrf(xmlhttp);
		}
		xmlhttp.send(body);
	}

// Get controls

	this.getControls = function(next) {
		// Return if already exists
		if(this.controls != null) {
			next(null, this.controls);
		} else {
			var thisAPI = this;
			this.request('GET', `${this.url + this.configID}/controls`, null, null, false, function(err, response) {
				// Errors
				if(err) return next(err);
				if(!response.success) return next(`Error getting controls, ${JSON.stringify(response, true, 3)}`);
				// Success
				thisAPI.controls = response.data;
				next(null, response.data);
			});
		}
	}

// Get control

	this.getControl = function(uuid, next) {
		this.getControls(function(err, controls) {
			for(var i = 0; i < controls.length; i++) {
				if(controls[i].uuid == uuid) {
					next(controls[i]);
				}
			}
		});
	}

// Get categories

	this.getCategories = function(next) {
		// Return if already exists
		if(this.categories != null) {
			next(null, this.categories);
		} else {
			var thisAPI = this;
			this.request('GET', `${this.url + this.configID}/categories`, null, null, false, function(err, response) {
				// Errors
				if(err) return next(err);
				if(!response.success) return next(`Error getting categories, ${JSON.stringify(response, true, 3)}`);
				// Success
				thisAPI.categories = response.data;
				next(null, response.data);
			});
		}
	}

// Get category

	this.getCategory = function(uuid, next) {
		this.getCategories(function(err, categories) {
			for(var i = 0; i < categories.length; i++) {
				if(categories[i].uuid == uuid) {
					next(categories[i]);
				}
			}
		});
	}

// Get rooms

	this.getRooms = function(next) {
		// Return if already exists
		if(this.rooms != null) {
			next(null, this.rooms);
		} else {
			var thisAPI = this;
			this.request('GET', `${this.url + this.configID}/rooms`, null, null, false, function(err, response) {
				// Errors
				if(err) return next(err);
				if(!response.success) return next(`Error getting rooms, ${JSON.stringify(response, true, 3)}`);
				// Success
				thisAPI.rooms = response.data;
				next(null, response.data);
			});
		}
	}

// Get room

	this.getRoom = function(uuid, next) {
		this.getRooms(function(err, rooms) {
			for(var i = 0; i < rooms.length; i++) {
				if(rooms[i].uuid == uuid) {
					next(rooms[i]);
				}
			}
		});
	}

// Get enables

	this.getEnables = function(next) {
		if(this.enables != null) {
			next(null, this.enables);
		} else {
			var thisAPI = this;
			this.request('GET', `${this.url + this.configID}/uuidsets/datum`, null, null, false, function(err, response) {
				// Errors
				if(err) return next(err);
				if(!response.success) return next(`Error getting enables, ${JSON.stringify(response, true, 3)}`);
				// Success
				thisAPI.enables = response.data;
				next(null, response.data);
			})
		}
	}

// Get enable

	this.getEnable = function(uuid, next) {
		this.getEnables(function(err, enables) {
			for(var e = 0; e < enables.length; e++) {
				if(enables[e] == uuid) {
					return next(true);
				}
			}
			next(false);
		})
	}

// Enable

	this.enable = function(uuid, next) {
		var body = JSON.stringify({ add: [uuid] });
		this.request('PATCH', `${this.url + this.configID}/uuidsets/datum`, {'Content-Type': 'application/json'}, body, true, next);
	}

// Disable

	this.disable = function(uuid, next) {
		var body = JSON.stringify({ remove: [uuid] });
		this.request('PATCH', `${this.url + this.configID}/uuidsets/datum`, {'Content-Type': 'application/json'}, body, true, next);
	}
