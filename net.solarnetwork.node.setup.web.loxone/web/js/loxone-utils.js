Loxone.element = function(options) {
  if(options.tag == null) options.tag = 'div';

  var element = document.createElement(options.tag);
  if(!!options.id) element.id = options.id;
  if(!!options.className) element.className = options.className;
  if(!!options.html) element.innerHTML = options.html;
  if(!!options.title) element.title = options.title;
  if(!!options.value) element.setAttribute('value', options.value);
  if(!!options.parent) options.parent.appendChild(element);

  return element;
}

Loxone.filterTable = function(table, filter, columns) {
  var result = [];
  filter = filter.toLowerCase();
  table.forEach(function(row) {
    for (var c = 0; c < columns.length; c++) {
      if(row[columns[c]] != null && row[columns[c]].toLowerCase().indexOf(filter) != -1) {
        return result.push(row);
      }
    }
  });

  return result;
}

Loxone.sortTable = function(table, column, ascending) {
  var remaining = table;
  var sorted = [];

  while(remaining.length > 0) {
    var best = 0;
    for (var r = 0; r < remaining.length; r++) {
      if(remaining[best] == null || (ascending && remaining[r][column] < remaining[best][column]) || (!ascending && remaining[r][column] > remaining[best][column])) {
        best = r;
      }
    }

    sorted.push(remaining[best]);
    remaining.splice(best, 1);
  }

  return sorted;
}

Loxone.formatDurationDate = function(date) {
  var now = new Date();
  date = new Date(date);

  var duration = now.getTime() - date.getTime();

  if(duration < 60000) {
    return `${Math.round(duration / 1000)} seconds ago`;
  } else if(duration < 3600000) {
    return `${Math.round(duration / 60000)} minutes ago`;
  } else if(duration < 86400000) {
    return `${Math.round(duration / 3600000)} hours ago`;
  } else {
    return `${Math.round(duration / 86400000)} days ago`;
  }

}
