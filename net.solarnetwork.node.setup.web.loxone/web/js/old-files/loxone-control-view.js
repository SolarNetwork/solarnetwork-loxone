var container = document.getElementById('device-view');

var api = new API();

var controller = null;

api.getRooms(function(err, roomList) {
  api.getCategories(function(err, categoryList) {
    api.getControls(function(err, controlList) {
      api.getEnables(function(err, enableList) {
        for (var c = 0; c < controlList.length; c++) {
          controlList[c].value = 0;
        }
        controller = new Controller(controlList, categoryList, roomList, enableList);
        websocket();
      });
    });
  });
});
