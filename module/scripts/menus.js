/* Add menu to extension bar */
ExtensionBar.addExtensionMenu({
  id: "extraction-extension",
  label: "Extraction",
  submenu: [
    {
      id   : "extraction-extension/configuration",
      label: "Configure extraction services...",
      click: dialogHandler(ConfigurationDialog),
    },
    { /* separator */ },
    {
      id   : "extraction-extension/about",
      label: "About...",
      click: dialogHandler(AboutDialog),
    },
  ]
});

/* Add submenu to column header menu */
DataTableColumnHeaderUI.extendMenu(function (column, columnHeaderUI, menu) {
  MenuSystem.appendTo(menu, "", [
    { /* separator */ },
    {
      id: "extraction/extract",
      label: "Extract elements...",
      click: dialogHandler(ExtractionDialog, column),
    },
  ]);
});

function dialogHandler(dialogConstructor) {
  var dialogArguments = Array.prototype.slice.call(arguments, 1);
  function Dialog() { return dialogConstructor.apply(this, dialogArguments); }
  Dialog.prototype = dialogConstructor.prototype;
  return function () { new Dialog().show(); };
}
