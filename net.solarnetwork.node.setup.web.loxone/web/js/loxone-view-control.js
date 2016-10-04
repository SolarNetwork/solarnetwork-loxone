Loxone.controlView = (function() {

  this.viewModel = {};

  this.sorting = { column: 'name', ascending: true };

  this.view = document.getElementById('control-view');

  this.append = function(control) {

    var row = this.element({ parent: this.view, className: 'loxone-row' });
    row.expanded = false;
    row.onclick = function(e) {
      if(e.target.tagName == 'INPUT') return;
      this.expanded = !this.expanded;
      if(this.expanded) {
        this.dropdown.style.display = 'block';
      } else {
        this.dropdown.style.display = 'none';
      }
    }

    this.element({ parent: row, className: 'loxone-name-column', html: control.name });
    this.element({ parent: row, className: 'loxone-category-column', html: control.cat });
    this.element({ parent: row, className: 'loxone-room-column', html: control.room });

    // Frequency
    var frequency = this.element({ parent: row, className: 'loxone-frequency-column' });
    var frequencyInput = this.element({ parent: frequency, tag: 'input', className: 'frequency-input' });
    // this.element({ parent: frequency, tag: 'span', className: 'frequency-label', html: 'seconds' });
    frequencyInput.uuid = control.uuid;
    frequencyInput.placeholder = 'default'
    frequencyInput.onchange = function() {

    }

    row.dropdown = this.controlView.appendDropdown(control);

  }

  this.appendDropdown = function(control) {
    var dropdown = this.element({ parent: this.view, className: 'dropdown' });

    for(s in control.states) {
      var row = this.element({ parent: dropdown, className: 'state-row' });

      var state = this.element({ parent: row, className: 'state-column', html: s });
      var value = this.element({ parent: row, className: 'value-column' });
      var time = this.element({ parent: row, className: 'time-column'  });

      // Value
      var valueID = `value-${control.states[s]}`;
      if(this.controlView.viewModel[valueID] == null) {
        this.controlView.viewModel[valueID] = ko.observable('-');
      }
      ko.applyBindingsToNode(value, { text: this.controlView.viewModel[valueID] });

      // Time
      var timeID = `time-${control.states[s]}`;
      if(this.controlView.viewModel[timeID] == null) {
        this.controlView.viewModel[timeID] = ko.observable('-');
      }
      ko.applyBindingsToNode(time, { text: this.controlView.viewModel[timeID] });


      // Enable button
      var enableRow = this.element({ parent: row, className: 'enable-column' });
      var enableButton = this.element({ parent: enableRow, tag: 'button', className: `enable-button${control.enabled ? ' enabled' : ''}`, html: 'Enable' });
      enableButton.enabled = control.enabled;
      enableButton.uuid = control.uuid;
      enableButton.onclick = function(evt) {
        var button = this;
        Loxone.api.setEnable(this.uuid, !button.enabled, function(err, response) {
          if(err) return console.log(err);
          if(response.success) {
            button.enabled = !button.enabled;
            button.className = `enable-button${button.enabled ? ' enabled' : ''}`;
          } else {
            console.error(`Error enabling - ${response.message}`);
          }
        });
      }

    }

    return dropdown;
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

            controls = this.filterTable(controls, filter, [ 'name', 'cat', 'room' ]);
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

  this.sortColumn = function(column) {
    if(this.controlView.sorting.column == column) {
      this.controlView.sorting.ascending = !this.controlView.sorting.ascending;
    } else {
      this.controlView.sorting.column = column;
      this.controlView.sorting.ascending = true;
    }
    this.controlView.draw();
    document.getElementById('sort-icon-name').className = 'sort-icon';
    document.getElementById('sort-icon-cat').className = 'sort-icon';
    document.getElementById('sort-icon-room').className = 'sort-icon';
    document.getElementById(`sort-icon-${column}`).className = `sort-icon ${this.controlView.sorting.ascending ? 'ascending' : 'descending'}`;
  }

  return this;
})();

Loxone.controlView.draw();
