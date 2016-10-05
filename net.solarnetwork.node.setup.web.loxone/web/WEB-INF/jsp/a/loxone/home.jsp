<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="loxone-heading">
	<div class="title">Device configuration</div>
  <input id="filter-input" type="text" placeholder="filter" onkeyup="Loxone.controlView.draw()">
  <div class="table-heading">
    <div class="loxone-name-column heading-column" onclick="Loxone.controlView.sortColumn('name')">
      <span class="name">Name</span>
      <div id="sort-icon-name" class="sort-icon ascending"></div>
    </div>
    <div class="loxone-type-column heading-column" onclick="Loxone.controlView.sortColumn('type')">
      <span class="name">Type</span>
      <div id="sort-icon-type" class="sort-icon"></div>
    </div>
    <div class="loxone-category-column heading-column" onclick="Loxone.controlView.sortColumn('cat')">
      <span class="name">Category</span>
      <div id="sort-icon-cat" class="sort-icon"></div>
    </div>
    <div class="loxone-room-column heading-column" onclick="Loxone.controlView.sortColumn('room')">
      <span class="name">Room</span>
      <div id="sort-icon-room" class="sort-icon"></div>
    </div>
    <div class="loxone-enable-column heading-column">
      <span class="name">Enable</span>
    </div>
    <div class="loxone-frequency-column heading-column">
      <span class="name">Frequency</span>
    </div>
  </div>
</div>

<div id="control-view" class="loxone-list"></div>

<script type="application/javascript" src="<c:url value='/js/loxone-view-control.js'/>"></script>
<script type="application/javascript" src="<c:url value='/js/loxone-websocket.js'/>"></script>
