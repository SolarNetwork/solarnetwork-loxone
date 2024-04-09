Loxone.api = (function() {
  this.request = function(req, next) {
    if(req.method == null) req.method = 'GET';
    if(req.path == null) req.path = '';
    if(req.url == null) req.url = this.url + req.path;

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

		xmlhttp.open(req.method, req.url, true);
		if(req.headers != null) {
			for(h in req.headers) {
				xmlhttp.setRequestHeader(h, req.headers[h]);
			}
		}

		if(req.csrf) { SolarNode.csrf(xmlhttp); }

		xmlhttp.send(req.body);

  }

  this.ping = function(next) {
    this.api.request({ url: SolarNode.context.path(`/a/loxone/ping`) }, next);
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
      next(uuid in enables);
    })
  }

  this.setEnable = function(uuid, enabled, next) {
    var body = JSON.stringify(enabled ? { add: [uuid] } : { remove: [uuid] });
    this.api.request({ method: 'PATCH', path: 'uuidsets/datum', headers: {'Content-Type': 'application/json'}, body: body, csrf: true }, function(err, response) {
      if(!err && response.success) {
        if(enabled) {
          if(!(uuid in Loxone.resources['uuidsets/datum'])) Loxone.resources['uuidsets/datum'][uuid] = null;
        } else {
          if(uuid in Loxone.resources['uuidsets/datum']) delete Loxone.resources['uuidsets/datum'][uuid];
        }
      }
      next(err, response);
    });
  }

  this.getFrequency = function(uuid, next) {
    this.getResourceList('uuidsets/datum', function(err, enables) {
      if(uuid in enables && saveFrequencySeconds in enables[uuid]) return next(enables[uuid].saveFrequencySeconds);
      next(null);
    })
  }

  this.setFrequency = function(uuid, frequency, next) {
    var body = { parameters: {} };
    body.parameters[uuid] = { saveFrequencySeconds: frequency };
    this.api.request({ method: 'PATCH', path: 'uuidsets/datum', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body), csrf: true }, function(err, response) {
      if(!err && response.success) {
        if(uuid in this.resources['uuidsets/datum']) {
          if(this.resources['uuidsets/datum'][uuid] === null) {
		    this.resources['uuidsets/datum'][uuid] = {};
		  }
          this.resources['uuidsets/datum'][uuid].saveFrequencySeconds = frequency;
        }
      }
    });
  }

  this.setDatumType = function(uuid, type, next) {
    var body = { add: [uuid], parameters: {} };
    body.parameters[uuid] = { datumValueType: type };
    this.api.request({ method: 'PATCH', path: 'uuidsets/props', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body), csrf: true }, function(err, response) {
      if(!err && response.success) {
        if(uuid in this.resources['uuidsets/props']) {
          if(this.resources['uuidsets/props'][uuid] === null) {
			this.resources['uuidsets/props'][uuid] = {};
		  }
          this.resources['uuidsets/props'][uuid].datumValueType = type;
        } else {
          this.resources['uuidsets/props'][uuid] = { datumValueType: type };
        }
      }
    });
  }

  this.getSources = function(next) {
    this.getResourceList('sources', next);
  }

  this.setSource = function(uuid, sourceID, next) {
    var body = { store: [{ uuid: uuid, sourceId: sourceID }] };
    this.api.request({ method: 'PATCH', path: 'sources', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body, true, 2), csrf: true }, next);
  }

  return this;

})();
