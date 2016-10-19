var Loxone = (function() {

  this.configID = document.querySelector('meta[name=\'loxone-config-id\']').getAttribute('content');

  this.url = SolarNode.context.path(`/a/loxone/${this.configID}/`);

  // Will contain controls, rooms, categories, data
  this.resources = {};

  return this;

})();
