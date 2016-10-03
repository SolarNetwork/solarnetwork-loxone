var Loxone = (function() {

  this.configID = document.querySelector('meta[name=\'loxone-config-ids\']').getAttribute('content');

  this.url = SolarNode.context.path(`/a/loxone/${this.configID}/`);

  this.resources = {};

  return this;

})();
