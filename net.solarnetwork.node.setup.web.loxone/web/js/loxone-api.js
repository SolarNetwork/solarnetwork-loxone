Loxone.api = (function() {
  this.request = function(req, next) {
    if(req.method == null) req.method = 'GET';
    if(req.path == null) req.path = '';
    if(req.url == null) req.url = this.url + req.path;

    var xmlhttp = new XMLHttpRequest();
		xmlhttp.onreadystatechange = function() {
			if(this.readyState == 4) {
				if(this.status == 200) {
          try {
  					next(null, JSON.parse(this.responseText));
          } catch (err) {
            console.log(`Error parsing JSON response: ${req.method} ${req.url}`);
            console.log(this.responseText);
            console.error(err);
          }
				} else {
					next(this.responseText);
				}
			}
		};

		xmlhttp.open(req.method, req.url, true);
		if(req.headers != null) {
			for(h in req.headers) {
				xmlhttp.setRequestHeader(h, req.headers[h]);
			}
		}

		if(req.csrf) { SolarNode.csrf(xmlhttp); }

		xmlhttp.send(req.body);

  }

  this.getResourceList = function(type, next) {
    if(this.resources[type] != null) return next(null, this.resources[type]);

    this.api.request({ path: type }, function(err, response) {
      if(err || !response.success) return next(`Error getting ${type}, ${err}, ${JSON.stringify(response)}`);

      Loxone.resources[type] = response.data;
      next(null, response.data);
    });
  }

  this.getResource = function(type, uuid, next) {
    this.getResourceList(type, function(err, list) {
			for(var i = 0; i < list.length; i++) {
				if(list[i].uuid == uuid) {
					return next(list[i]);
				}
			}
      next(null);
		});
  }

  this.isEnabled = function(uuid, next) {
    this.getResourceList('uuidsets/datum', function(err, enables) {
      next(!!enables[uuid]);
    })
  }

  this.setEnable = function(uuid, enabled, next) {
    var body = JSON.stringify(enabled ? { add: [uuid] } : { remove: [uuid] });
    this.api.request({ method: 'PATCH', path: 'uuidsets/datum', headers: {'Content-Type': 'application/json'}, body: body, csrf: true }, next);
  }

// Set frequency

	this.setFrequency = function(uuid, frequency, next) {
		var json = { parameters: {} };
		json.parameters[uuid] = { saveFrequencySeconds: frequency };
		var body = JSON.stringify(json);
		this.request('PATCH', `${this.url + this.configID}/uuidsets/datum`, {'Content-Type': 'application/json'}, body, true, next);
	}


  return this;

})();
