var logger = Packages.org.slf4j.LoggerFactory.getLogger("extraction-extension"),
    File = Packages.java.io.File,
    refineServlet = Packages.com.google.refine.RefineServlet,
    extraction = Packages.com.brainymachine.extraction,
    services = extraction.services,
    commands = extraction.commands;

/* Initialize the extension. */
function init() {
  logger.info("Initializing service manager");
  var cacheFolder = new refineServlet().getCacheDir("extraction-extension");
  var serviceManager = new services.ExtractionServiceManager(new File(cacheFolder + "/services.json"));
  
  logger.info("Initializing commands");
  register("services", new commands.ServicesCommand(serviceManager));
  register("extractions", new commands.ExtractionCommand(serviceManager));
  
  logger.info("Initializing client resources");
  var resourceManager = Packages.com.google.refine.ClientSideResourceManager;
  resourceManager.addPaths(
    "project/scripts",
    module, [
      "scripts/config.js",
      "scripts/util.js",
      "scripts/dialogs/about.js",
      "scripts/dialogs/configuration.js",
      "scripts/dialogs/extraction.js",
      "scripts/menus.js",
    ]
  );
  resourceManager.addPaths(
    "project/styles",
    module, [
      "styles/main.less",
      "scripts/dialogs/dialogs.less",
      "scripts/dialogs/about.less",
      "scripts/dialogs/configuration.less",
      "scripts/dialogs/extraction.less",
    ]
  );
}

function register(path, command) {
  refineServlet.registerCommand(module, path, command);
}
