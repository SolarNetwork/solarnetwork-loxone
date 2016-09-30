Loxone.element = function(options) {
  if(options.tag == null) options.tag = 'div';

  var element = document.createElement(options.tag);
  if(options.id != null) element.id = options.id;
  if(options.className != null) element.className = options.className;
  if(options.html != null) element.innerHTML = options.html;
  if(options.parent != null) options.parent.appendChild(element);

  return element;
}

Loxone.filterTable = function(table, filter, columns) {
  var result = [];
  table.forEach(function(row) {
    for (var c = 0; c < columns.length; c++) {
      if(row[columns[c]] != null && row[columns[c]].indexOf(filter) != -1) {
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
