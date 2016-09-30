<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="loxone-heading">
	<div class="title">Device configuration</div>
  <input id="filter-input" type="text" placeholder="filter" onkeyup="controller.loadControls()">
  <div class="table-heading">
    <div class="name-column heading-column" onclick="controller.sortControls('name')">
      <span class="name">Name</span>
      <div id="sort-icon-name" class="sort-icon ascending"></div>
    </div>
    <div class="category-column heading-column" onclick="controller.sortControls('category')">
      <span class="name">Category</span>
      <div id="sort-icon-category" class="sort-icon"></div>
    </div>
    <div class="room-column heading-column" onclick="controller.sortControls('room')">
      <span class="name">Room</span>
      <div id="sort-icon-room" class="sort-icon"></div>
    </div>
    <div class="value-column heading-column" onclick="controller.sortControls('value')">
      <span class="name">Value</span>
      <div id="sort-icon-value" class="sort-icon"></div>
    </div>
    <div class="enable-column heading-column">
      <span class="name">Enable</span>
      <div id="sort-icon-enable" class="sort-icon"></div>
    </div>
  </div>
</div>

<div id="control-view" class="loxone-list"></div>

<script type="application/javascript" src="<c:url value='/js/loxone-view-control.js'/>"></script>
