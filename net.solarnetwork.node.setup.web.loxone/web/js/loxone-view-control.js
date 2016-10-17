Loxone.controlView = (function() {

  this.viewModel = { enableFilter: ko.observable('All') };

  ko.applyBindingsToNode(document.getElementById('loxone-enable-text'), { text: this.viewModel.enableFilter });

  this.sorting = { column: 'name', ascending: true, enable: 'All' };

  this.view = document.getElementById('control-view');

  this.append = function(control) {

    var enabled = control.uuid in this.datums;

    var row = this.element({ parent: this.view, id: `loxone-row-${control.uuid}`, className: `loxone-row${enabled ? ' enabled-row' : ''}` });
    row.expanded = false;
    row.onclick = function(e) {
      if(e.target.tagName == 'INPUT' || e.target.className.indexOf('enable-toggle') != -1 || e.target.className.indexOf('frequency-display') != -1) return;
      this.expanded = !this.expanded;
      if(this.expanded) {
        this.dropdown.style.display = 'block';
      } else {
        this.dropdown.style.display = 'none';
      }
    }

    // Enable toggle
    var enableColumn = this.element({ parent: row, className: 'loxone-enable-column' });
    var enableToggle = this.element({ parent: enableColumn, tag: 'div', id: `loxone-toggle-${control.uuid}`, className: `enable-toggle${enabled ? ' enabled' : ''}` });
    enableToggle.row = row;
    enableToggle.rowType = 'control';
    enableToggle.enabled = enabled;
    enableToggle.uuid = control.uuid;
    enableToggle.onclick = this.selectEnable;

    row.dropdown = this.controlView.appendDropdown(control);

    this.element({ parent: row, className: 'loxone-name-column', html: control.name });
    this.element({ parent: row, className: 'loxone-source-column', html: control.source });
    this.element({ parent: row, className: 'loxone-type-column', html: control.type });
    this.element({ parent: row, className: 'loxone-category-column', html: control.cat });
    this.element({ parent: row, className: 'loxone-room-column', html: control.room });

    // Frequency
    var frequency = this.element({ parent: row, className: 'loxone-frequency-column' });
    var frequencyDisplay = this.element({ parent: frequency, tag: 'div', className: 'frequency-display' });
    var frequencyInput = this.element({ parent: frequency, tag: 'input', className: 'frequency-input' });
    frequencyInput.uuid = control.uuid;
    frequencyInput.placeholder = 'default'
    frequencyInput.onblur = this.changeFrequency;
    frequencyInput.displayElement = frequencyDisplay;
    frequencyInput.onkeypress = function(e) {
      if(e.keyCode == 13) this.blur();
    }
    frequencyDisplay.inputElement = frequencyInput;
    frequencyDisplay.onclick = this.selectFrequency;

    var frequencyID = `frequency-${control.uuid}`;
    if(this.controlView.viewModel[frequencyID] == null) {
      var frequencyValue = (!!this.datums[control.uuid] && 'saveFrequencySeconds' in this.datums[control.uuid]) ? this.datums[control.uuid].saveFrequencySeconds : '0';
      this.controlView.viewModel[frequencyID] = ko.observable(frequencyValue);
    }
    ko.applyBindingsToNode(frequencyDisplay, { text: this.controlView.viewModel[frequencyID] });
    ko.applyBindingsToNode(frequencyInput, { value: this.controlView.viewModel[frequencyID] });
  }

  this.appendDropdown = function(control) {
    var dropdown = this.element({ parent: this.view, className: 'dropdown' });

    for(s in control.states) {
      var enabled = control.states[s] in this.datums;

      var row = this.element({ parent: dropdown, id: `state-row-${control.states[s]}`, className: `state-row${enabled ? ' enabled-row' : ''}` });

      var state = this.element({ parent: row, className: 'state-column', html: s });

      // Enable toggle
      var enableColumn = this.element({ parent: row, className: 'enable-column' });
      var enableToggle = this.element({ parent: enableColumn, tag: 'div', id: `state-toggle-${control.states[s]}`, className: `enable-toggle${enabled ? ' enabled' : ''}` });
      enableToggle.row = row;
      enableToggle.rowType = 'state';
      enableToggle.enabled = enabled;
      enableToggle.uuid = control.states[s];
      enableToggle.onclick = this.selectEnable;

      var datumOptions = [ 'Unknown', 'Instantaneous', 'Accumulating', 'Status' ];
      var datumType = this.element({ parent: row, tag: 'select', className: 'datum-type-select' });
      datumOptions.forEach(function(option) {
        this.element({ parent: datumType, tag: 'option', html: option, value: option });
      })
      datumType.uuid = control.states[s];
      datumType.onchange = this.selectDatumType;
      if(!!this.props[control.states[s]] && !!this.props[control.states[s]].datumValueType) {
        datumType.selectedIndex = datumOptions.indexOf(this.props[control.states[s]].datumValueType);
      }

      var time = this.element({ parent: row, className: 'time-column'  });

      // Time
      var timeID = `time-${control.states[s]}`;
      if(this.controlView.viewModel[timeID] == null) {
        this.controlView.viewModel[timeID] = ko.observable('-');
      }
      ko.applyBindingsToNode(time, { text: this.controlView.viewModel[timeID] });

      var value = this.element({ parent: row, className: 'value-column' });

      // Value
      var valueID = `value-${control.states[s]}`;
      if(this.controlView.viewModel[valueID] == null) {
        this.controlView.viewModel[valueID] = ko.observable('-');
      }
      ko.applyBindingsToNode(value, { text: this.controlView.viewModel[valueID] });

    }

    return dropdown;
  }

  this.draw = function() {

    this.view.innerHTML = '';

    this.api.getResourceList('categories', function(err, categories) {
      if(err) return console.log(err);
      this.api.getResourceList('rooms', function(err, rooms) {
        if(err) return console.log(err);
        this.api.getResourceList('uuidsets/datum', function(err, datums) {
          if(err) return console.log(err);
          this.api.getResourceList('uuidsets/props', function(err, props) {
            if(err) return console.log(err);
            this.api.getResourceList('sources', function(err, sources) {
              if(err) return console.log(err);

              this.datums = datums;
              this.props = props;

              this.sources = {};

              sources.forEach(function(source) {
                this.sources[source.uuid] = source.sourceId;
              });

              this.api.getResourceList('controls', function(err, controls) {
                if(err) return console.log(err);

                for (var c = 0; c < controls.length; c++) {
                  for (var i = 0; i < categories.length; i++) {
                    if(controls[c].cat == categories[i].uuid){ controls[c].cat = categories[i].name; break; }
                  }
                  for (var i = 0; i < rooms.length; i++) {
                    if(controls[c].room == rooms[i].uuid){ controls[c].room = rooms[i].name; break; }
                  }
                  for(var i = 0; i < sources.length; i++) {
                    if(controls[c].uuid == sources[i].uuid) { controls[c].source = sources[i].name; }
                  }
                }

                var filter = document.getElementById('filter-input').value.toLowerCase();

                controls = this.filterTable(controls, filter, [ 'name', 'source', 'type', 'cat', 'room' ]);
                controls = this.sortTable(controls, this.controlView.sorting.column, this.controlView.sorting.ascending);

                for (var c = 0; c < controls.length; c++) {
                  var enabled = controls[c].uuid in datums;
                  var enableFilter = this.controlView.sorting.enable;
                  if(enableFilter == 'All' || (enableFilter == 'Enabled' && enabled) || (enableFilter == 'Disabled' && !enabled)) {
                    append(controls[c]);
                  }
                }
              });
            });
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
    document.getElementById('sort-icon-source').className = 'sort-icon';
    document.getElementById('sort-icon-type').className = 'sort-icon';
    document.getElementById('sort-icon-cat').className = 'sort-icon';
    document.getElementById('sort-icon-room').className = 'sort-icon';
    document.getElementById(`sort-icon-${column}`).className = `sort-icon ${this.controlView.sorting.ascending ? 'ascending' : 'descending'}`;
  }

  this.selectEnable = function() {
    var toggle = this;
    Loxone.api.setEnable(toggle.uuid, !toggle.enabled, function(err, response) {
      if(err) return console.log(err);
      if(!response.success) return console.error(`Error enabling - ${response.message}`);

      toggle.enabled = !toggle.enabled;
      toggle.className = `enable-toggle${toggle.enabled ? ' enabled' : ''}`;
      var controlToggle = document.getElementById(`loxone-toggle-${toggle.uuid}`);
      if(controlToggle) {
        controlToggle.className = `enable-toggle${toggle.enabled ? ' enabled' : ''}`;
        controlToggle.enabled = toggle.enabled;
      }
      var stateToggle = document.getElementById(`state-toggle-${toggle.uuid}`);
      if(stateToggle){
        stateToggle.className = `enable-toggle${toggle.enabled ? ' enabled' : ''}`;
        stateToggle.enabled = toggle.enabled;
      }
      // toggle.row.className = `${toggle.rowType == 'control' ? 'loxone-row' : 'state-row'}${toggle.enabled ? ' enabled-row' : ''}`;
      var loxoneRow = document.getElementById(`loxone-row-${toggle.uuid}`);
      if(loxoneRow) loxoneRow.className = `loxone-row${toggle.enabled ? ' enabled-row' : ''}`;
      var stateRow = document.getElementById(`state-row-${toggle.uuid}`);
      if(stateRow) stateRow.className = `state-row${toggle.enabled ? ' enabled-row' : ''}`;
    });
  }

  this.changeFrequency = function() {
    this.style.display = 'none';
    this.displayElement.style.display = 'block';
    Loxone.api.setFrequency(this.uuid, parseInt(this.value), function(err, response) {
      if(err) return console.error(err);
      if(!response.success) console.error(response);
      console.log(response);
    })
  }

  this.selectFrequency = function() {
    this.style.display = 'none';
    this.inputElement.style.display = 'block';
    this.inputElement.focus();
    this.inputElement.setSelectionRange(0, this.inputElement.value.length)
  }

  this.selectDatumType = function() {
    Loxone.api.setDatumType(this.uuid, this.value, function(err, response) {
      if(err) console.error(err);
      if(!response.success) console.error(response);
    });
  }

  this.openEnableFilter = function() {
    var element = document.getElementById('loxone-enable-filter');
    element.className = 'enable-filter-dropdown visible';
    element.focus();
    element.onblur = Loxone.controlView.closeEnableFilter;
  }

  this.closeEnableFilter = function() {
    document.getElementById('loxone-enable-filter').className = 'enable-filter-dropdown';
  }

  this.enableFilter = function(filter) {
    this.controlView.sorting.enable = filter;
    this.controlView.closeEnableFilter();
    this.controlView.draw();
    Loxone.controlView.viewModel.enableFilter(filter);
  }

  this.submitFile = function() {
    var files = document.getElementById('loxone-file-input').files;
    if(files.length > 0) {

      var formData = new FormData(document.getElementById('loxone-file-input'));
      // formData.append("CustomField", "This is some extra data");
      $.ajax({
        url: this.url + 'sources',
        type: "POST",
        data: formData,
        contentType: null,
        processData: false
      });

      // var ext = '.xml';
      // // Make sure the file extension is .xml
      // if(files[0].name.indexOf(ext, files[0].name.length - ext.length) == -1) {
      //   SolarNode.error('Config uploads require the extension ".xml", please rename your file before uploading');
      // } else {
      //   document.getElementById('loxone-file-upload-form').submit();
      // }

      // $('#loxone-file-upload-form').ajaxSubmit({
      //   beforeSubmit: function(arr, $form, options) {
      //     console.log($form[0])
      //     // $form[0].files[0].name += '.xml',
      //     // console.log($form[0].files);
      //     // return false;
      //   }
      // })
    }
  }

  this.openFileInput = function() {
    var input = document.getElementById('loxone-file-input');
    input.click();
  }

  return this;
})();

Loxone.controlView.draw();
