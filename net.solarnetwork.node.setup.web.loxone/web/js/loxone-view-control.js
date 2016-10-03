Loxone.controlView = (function() {

  this.viewModel = {};

  this.sorting = { column: 'name', ascending: true };

  this.view = document.getElementById('control-view');

  this.append = function(control) {
    var row = this.element({ parent: this.view, className: 'loxone-row' });

    this.element({ parent: row, className: 'name-column', html: control.name });
    this.element({ parent: row, className: 'category-column', html: control.cat });
    this.element({ parent: row, className: 'room-column', html: control.room });
    var value = this.element({ parent: row, className: 'value-column' });

    var valueID = `value-${control.uuid}`;

    if(this.controlView.viewModel[valueID] == null) {
      this.controlView.viewModel[valueID] = ko.observable('-');
    }

    ko.applyBindingsToNode(value, { text: this.controlView.viewModel[valueID] });

    var enableRow = this.element({ parent: row, className: 'enabled-column' })
    var enableButton = this.element({ parent: enableRow, tag: 'button', className: `enable-button${control.enabled ? ' enabled' : ''}`, html: 'Enable' });
    enableButton.enabled = control.enabled;
    enableButton.uuid = control.uuid;
    enableButton.onclick = function(evt) {
      var button = this;
      Loxone.api.setEnable(this.uuid, !button.enabled, function(err, response) {
        if(err) return console.log(err);
        if(response.success) {
          console.log('Enabled');
          button.enabled = !button.enabled;
          button.className = `enable-button${button.enabled ? ' enabled' : ''}`;
        } else {
          console.error(`Error enabling - ${response.message}`);
        }
      });
    }
  }

  this.draw = function() {

    this.view.innerHTML = '';

    this.api.getResourceList('categories', function(err, categories) {
      if(err) return console.log(err);
      this.api.getResourceList('rooms', function(err, rooms) {
        if(err) return console.log(err);
        this.api.getResourceList('uuidsets/datum', function(err, enables) {
          if(err) return console.log(err);
          this.api.getResourceList('controls', function(err, controls) {
            if(err) return console.log(err);

            for (var c = 0; c < controls.length; c++) {
              for (var i = 0; i < categories.length; i++) {
                if(controls[c].cat == categories[i].uuid){ controls[c].cat = categories[i].name; break; }
              }
              for (var i = 0; i < rooms.length; i++) {
                if(controls[c].room == rooms[i].uuid){ controls[c].room = rooms[i].name; break; }
              }
              controls[c].enabled = !!enables[controls[c].uuid];
            }

            var filter = document.getElementById('filter-input').value.toLowerCase();

            controls = this.filterTable(controls, filter, [ 'name' ]);
            controls = this.sortTable(controls, this.controlView.sorting.column, this.controlView.sorting.ascending);

            for (var c = 0; c < controls.length; c++) {
              append(controls[c]);
            }
          });
        });
      });
    });
  }

  this.findStateControl = function(uuid, next) {
    this.api.getResourceList('controls', function(err, controls) {
      if(err) return next('');
      for (var c = 0; c < controls.length; c++) {
        if(controls[c].states) {
          for (var s = 0; s < controls[c].states.length; s++) {
            if(uuid == controls[c].states[s]) {
              return next(controls[c].uuid);
            }
          }
        }
      }
      next('');
    });
  }

  return this;
})();

Loxone.controlView.draw();
