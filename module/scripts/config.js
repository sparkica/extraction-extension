/* Expose configuration */
var ExtractionExtension = {};
ExtractionExtension.commandPath = "/command/extraction-extension/";
ExtractionExtension.servicesPath = ExtractionExtension.commandPath + "services";

// Register a dummy reconciliation service that will be used to display named entities
ReconciliationManager.registerService({
  name: "Element Extraction Service",
  url: "ExtractedElement",
  // By setting the URL to "{{id}}",
  // this whole string will be replaced with the actual URL
  view: { url: "{{id}}" },
});
