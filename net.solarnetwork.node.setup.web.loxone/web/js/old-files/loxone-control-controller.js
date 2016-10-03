function Controller(controls, categories, rooms, enables) {
  this.controls = controls;
  this.categories = categories;
  this.rooms = rooms;
  this.enables = enables;

  this.container = document.getElementById('device-view');

  this.sorting = { column: 'name', ascending: true };

  this.setView = function(view) {
    if(typeof view == 'string') {
      this.view = document.getElementById(view);
    } else {
      this.view = view;
    }
  }

  this.getControl = function(uuid) {
    for (var i = 0; i < this.controls.length; i++) {
      if(this.controls[i].uuid == uuid) return this.controls[i];
    }
  }

  this.getCategory = function(uuid) {
    for (var i = 0; i < this.categories.length; i++) {
      if(this.categories[i].uuid == uuid) return this.categories[i];
    }
  }

  this.getRoom = function(uuid) {
    for (var i = 0; i < this.rooms.length; i++) {
      if(this.rooms[i].uuid == uuid) return this.rooms[i];
    }
  }

  this.enable = function(uuid) {
     console.log('Enable');
    this.enables.push(uuid);
    var button = document.getElementById(`enable-${uuid}`);
    button.className = 'enable-button enabled';
    button.enabled = true;
  }

  this.disable = function(uuid) {
    console.log('Disable');
    if(this.isEnabled(uuid)) {
      this.enables.splice(this.enables.indexOf(uuid), 1);
    }
    var button = document.getElementById(`enable-${uuid}`)
    button.className = 'enable-button';
    button.enabled = false;
  }

  this.isEnabled = function(uuid) {
    return this.enables.indexOf(uuid) != -1;
  }

  this.appendElement = function(parent, tag, id, className, html) {
    var element = document.createElement(tag);
    if(id != null) element.id = id;
    if(className != null) element.className = className;
    if(html != null) element.innerHTML = html;
    if(parent != null) parent.appendChild(element);
    return element;
  }


  this.loadControls = function() {
    var controls = this.sortControlTable();
    var categories = this.categories;
    var rooms = this.rooms;

    this.container.innerHTML = '';

    var thisController = this;

    // Append controls
    for (var c = 0; c < controls.length; c++) {
      var row = document.createElement('div');
      row.className = 'row';

      this.appendElement(row, 'div', null, 'name-column', controls[c].name);
      this.appendElement(row, 'div', null, 'category-column', this.getCategory(controls[c].cat).name);
      this.appendElement(row, 'div', null, 'room-column', this.getRoom(controls[c].room).name);
      this.appendElement(row, 'div', `value-${controls[c].uuid}`, 'value-column', controls[c].value);
      var enableRow = this.appendElement(row, 'div', null, 'enabled-column', null);
      var enableButton = this.appendElement(enableRow, 'button', `enable-${controls[c].uuid}`, `enable-button${this.isEnabled(controls[c].uuid) ? ' enabled' : ''}`, 'Enable');
      enableButton.uuid = controls[c].uuid;
      enableButton.enabled = this.isEnabled(controls[c].uuid);
      enableButton.onclick = function(evt) {
        var uuid = evt.target.uuid;
        if(this.enabled) {
          api.disable(uuid, function(err, response) {
            if(err) return console.log(`Error disabling button ${err}`);
            if(response.success) thisController.disable(uuid);
          })
        } else {
          api.enable(uuid, function(err, response) {
            if(err) return console.log(`Error enabling button ${err}`);
            if(response.success) thisController.enable(uuid);
          })
        }
      }

      this.container.appendChild(row);
    }
  }

  this.sortControls = function(column) {
    if(this.sorting.column == column) {
      this.sorting.ascending = !this.sorting.ascending;
    } else {
      this.sorting.column = column;
      this.sorting.ascending = true;
    }

    document.getElementById('sort-icon-name').className = 'sort-icon';
    document.getElementById('sort-icon-category').className = 'sort-icon';
    document.getElementById('sort-icon-room').className = 'sort-icon';
    document.getElementById('sort-icon-value').className = 'sort-icon';

    var icon = document.getElementById(`sort-icon-${column}`);
    if(icon) icon.className = `sort-icon ${this.sorting.ascending ? 'ascending' : 'descending'}`;

    this.loadControls();
  }

  this.loadControls();
}

Controller.prototype.sortControlTable = function() {
  var filter = document.getElementById('filter-input').value.toLowerCase();

  var remaining = [];

  for (var c = 0; c < this.controls.length; c++) {
    if(filter == null || filter == '') {
      remaining.push(this.controls[c]);
    } else {
      var name = this.controls[c].name.toLowerCase();
      var category = this.getCategory(this.controls[c].cat).name.toLowerCase();
      var room = this.getRoom(this.controls[c].room).name.toLowerCase();

      if(filter == null || filter == '' || name.indexOf(filter) != -1 || category.indexOf(filter) != -1 || room.indexOf(filter) != -1) {
        remaining.push(this.controls[c]);
      }
    }
  }

  var sorted = [];

  while(remaining.length > 0) {
    var best = 0;
    for(r in remaining) {
      switch(this.sorting.column) {
        case 'name':
          if((this.sorting.ascending && remaining[r].name < remaining[best].name) || (!this.sorting.ascending && remaining[r].name > remaining[best].name)) {
            best = r;
          }
          break;
        case 'category':
          var category = this.getCategory(remaining[r].cat).name;
          var bestCategory = this.getCategory(remaining[best].cat).name;
          if((this.sorting.ascending && category < bestCategory) || (!this.sorting.ascending && category > bestCategory)) {
            best = r;
          }
          break;
        case 'room':
          var room = this.getRoom(remaining[r].room).name;
          var bestCategory = this.getRoom(remaining[best].room).name;
          if((this.sorting.ascending && room < bestCategory) || (!this.sorting.ascending && room > bestCategory)) {
            best = r;
          }
          break;
        case 'value':
          if((this.sorting.ascending && remaining[r].value < remaining[best].value) || (!this.sorting.ascending && remaining[r].value > remaining[best].value)) {
            best = r;
          }
          break;
      }
    }
    sorted.push(remaining[best]);
    remaining.splice(best, 1);
  }

  return sorted;
}
